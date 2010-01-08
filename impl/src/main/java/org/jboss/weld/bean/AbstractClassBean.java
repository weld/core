/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bean;

import static org.jboss.weld.logging.Category.BEAN;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_CONTAINER_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.ONLY_ONE_SCOPE_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;
import static org.jboss.weld.logging.messages.BeanMessage.USING_DEFAULT_SCOPE;
import static org.jboss.weld.logging.messages.BeanMessage.USING_SCOPE;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Scope;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.interceptor.model.InterceptorClassMetadataImpl;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.interceptor.util.proxy.TargetInstanceProxy;
import org.jboss.weld.bean.proxy.DecoratorProxyMethodHandler;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.SerializableContextualImpl;
import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Strings;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.reflection.SecureReflections;
import org.slf4j.cal10n.LocLogger;

/**
 * An abstract bean representation common for class-based beans
 * 
 * @author Pete Muir
 * @author David Allen
 * 
 * @param <T> the type of class for the bean
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>>
{
   // Logger
   private static final LocLogger log = loggerFactory().getLogger(BEAN);

   // The item representation
   protected WeldClass<T> annotatedItem;

   // The injectable fields of each type in the type hierarchy, with the actual type at the bottom 
   private List<Set<FieldInjectionPoint<?, ?>>> injectableFields;

   // The initializer methods of each type in the type hierarchy, with the actual type at the bottom
   private List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;

   // Decorators
   private List<Decorator<?>> decorators;
   private Class<T> proxyClassForDecorators;
   private final ThreadLocal<Integer> decoratorStackPosition;
   private final ThreadLocal<T> decoratedActualInstance = new ThreadLocal<T>();

   // Interceptors
   private boolean hasSerializationOrInvocationInterceptorMethods;

   // Bean callback methods
   private WeldMethod<?, ?> postConstruct;
   private WeldMethod<?, ?> preDestroy;
   
   // Injection target for the bean
   private InjectionTarget<T> injectionTarget;

   private ConstructorInjectionPoint<T> constructor;

   /**
    * Constructor
    * 
    * @param type The type
    * @param manager The Bean manager
    */
   protected AbstractClassBean(WeldClass<T> type, String idSuffix, BeanManagerImpl manager)
   {
      super(idSuffix, manager);
      this.annotatedItem = type;
      this.decoratorStackPosition = new ThreadLocal<Integer>()
      {
         
         @Override
         protected Integer initialValue()
         {
            return 0;
         }
         
      };
      initStereotypes();
      initAlternative();
      initInitializerMethods();
      initInjectableFields();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      super.initialize(environment);
      checkBeanImplementation();
      if (isInterceptionCandidate())
      {
            initCdiBoundInterceptors();
            initDirectlyDefinedInterceptors();
      }
   }

   @Override
   public void initializeAfterBeanDiscovery()
   {
      super.initializeAfterBeanDiscovery();
      initDecorators();
      if (hasDecorators())
      {
         initProxyClassForDecoratedBean();
      }
   }

   @Override
   public void checkType()
   {
      
   }

   public void initDecorators()
   {
      this.decorators = getManager().resolveDecorators(getTypes(), getQualifiers());
   }

   public boolean hasDecorators()
   {
      return this.decorators != null && this.decorators.size() > 0;
   }

   protected void initProxyClassForDecoratedBean()
   {
      this.proxyClassForDecorators = Proxies.createProxyClass(TypeInfo.of(getTypes()).add(TargetInstanceProxy.class));
   }

   protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint)
   {
      List<SerializableContextualInstance<Decorator<Object>, Object>> decoratorInstances = new ArrayList<SerializableContextualInstance<Decorator<Object>,Object>>();
      InjectionPoint ip = originalInjectionPoint;
      boolean outside = decoratorStackPosition.get().intValue() == 0;
      if (outside)
      {
         decoratedActualInstance.set(instance);
      }

      try
      {
         int i = decoratorStackPosition.get();
         while (i < decorators.size())
         {
            Decorator<?> decorator = decorators.get(i);
               decoratorStackPosition.set(++i);

            Object decoratorInstance = getManager().getReference(ip, decorator, creationalContext);
            decoratorInstances.add(new SerializableContextualInstanceImpl<Decorator<Object>, Object>((Decorator<Object>) decorator, decoratorInstance, null));
            
            ip = Beans.getDelegateInjectionPoint(decorator);
            if (ip == null)
            {
               throw new ForbiddenStateException(NON_CONTAINER_DECORATOR, decorator);
            }
         }
      }
      finally
      {
         if (outside)
         {
            decoratorStackPosition.remove();
         }
      }
      try
      {
         T proxy = SecureReflections.newInstance(proxyClassForDecorators);
         // temporary fix for decorators - make sure that the instance wrapped by the decorators
         // is the contextual instance
         // TODO - correct the decoration algorithm to avoid the creation of new target class instances
         Proxies.attachMethodHandler(proxy, new DecoratorProxyMethodHandler(decoratorInstances, decoratedActualInstance.get()));
         return proxy;
      }
      catch (InstantiationException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
      finally
      {
         if (outside)
         {
            decoratedActualInstance.set(null);
         }
      }
   }
   
   public List<Decorator<?>> getDecorators()
   {
      return Collections.unmodifiableList(decorators);
   }

   /**
    * Initializes the bean type
    */
   protected void initType()
   {
      this.type = getAnnotatedItem().getJavaClass();
   }

   /**
    * Initializes the injection points
    */
   protected void initInjectableFields()
   {
      injectableFields = Beans.getFieldInjectionPoints(this, annotatedItem);
      addInjectionPoints(Beans.getFieldInjectionPoints(this, injectableFields));
   }

   /**
    * Initializes the initializer methods
    */
   protected void initInitializerMethods()
   {
      initializerMethods = Beans.getInitializerMethods(this, getAnnotatedItem());
      addInjectionPoints(Beans.getParameterInjectionPoints(this, initializerMethods));
   }

   @Override
   protected void initScopeType()
   {
      for (WeldClass<?> clazz = getAnnotatedItem(); clazz != null; clazz = clazz.getWeldSuperclass())
      {
         Set<Annotation> scopeTypes = new HashSet<Annotation>();
         scopeTypes.addAll(clazz.getDeclaredMetaAnnotations(Scope.class));
         scopeTypes.addAll(clazz.getDeclaredMetaAnnotations(NormalScope.class));
         if (scopeTypes.size() == 1)
         {
            if (getAnnotatedItem().isAnnotationPresent(scopeTypes.iterator().next().annotationType()))
            {
               this.scopeType = scopeTypes.iterator().next().annotationType();
               log.trace(USING_SCOPE, scopeType, this);
            }
            break;
         }
         else if (scopeTypes.size() > 1)
         {
            throw new DefinitionException(ONLY_ONE_SCOPE_ALLOWED, getAnnotatedItem());
         }
      }

      if (this.scopeType == null)
      {
         initScopeTypeFromStereotype();
      }

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace(USING_DEFAULT_SCOPE, this);
      }
   }

   /**
    * Validates the bean implementation
    */
   protected void checkBeanImplementation() {}

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      if (getAnnotatedItem().getWeldSuperclass() == null || getAnnotatedItem().getWeldSuperclass().getJavaClass().equals(Object.class))
      {
         throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
      }
   }

   /**
    * Gets the annotated item
    * 
    * @return The annotated item
    */
   @Override
   public WeldClass<T> getAnnotatedItem()
   {
      return annotatedItem;
   }

   /**
    * Gets the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      String name = Strings.decapitalize(getAnnotatedItem().getSimpleName());
      return name;
   }

   /**
    * Gets the annotated methods
    * 
    * @return The set of annotated methods
    */
   public List<? extends Set<? extends MethodInjectionPoint<?, ?>>> getInitializerMethods()
   {
      // TODO Make immutable
      return initializerMethods;
   }
   
   /**
    * @return the injectableFields
    */
   public List<? extends Set<FieldInjectionPoint<?, ?>>> getInjectableFields()
   {
      // TODO Make immutable
      return injectableFields;
   }


   /**
    * Initializes the post-construct method
    */
   protected void initPostConstruct()
   {
      this.postConstruct = Beans.getPostConstruct(getAnnotatedItem());
   }

   /**
    * Initializes the pre-destroy method
    */
   protected void initPreDestroy()
   {
      this.preDestroy = Beans.getPreDestroy(getAnnotatedItem());
   }

   /**
    * Returns the post-construct method
    * 
    * @return The post-construct method
    */
   public WeldMethod<?, ?> getPostConstruct()
   {
      return postConstruct;
   }

   /**
    * Returns the pre-destroy method
    * 
    * @return The pre-destroy method
    */
   public WeldMethod<?, ?> getPreDestroy()
   {
      return preDestroy;
   }

    protected abstract boolean isInterceptionCandidate();

   /**
    * Extracts the complete set of interception bindings from a given set of annotations.
    *
    * @param manager
    * @param annotations
    * @return
    */
   protected static Set<Annotation> flattenInterceptorBindings(BeanManagerImpl manager, Set<Annotation> annotations)
   {
      Set<Annotation> foundInterceptionBindingTypes = new HashSet<Annotation>();
      for (Annotation annotation: annotations)
      {
         if (manager.isInterceptorBinding(annotation.annotationType()))
         {
            foundInterceptionBindingTypes.add(annotation);
            foundInterceptionBindingTypes.addAll(manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotation.annotationType()).getInheritedInterceptionBindingTypes());
         }
      }
      return foundInterceptionBindingTypes;
   }

   protected void initCdiBoundInterceptors()
   {
      if (manager.getCdiInterceptorsRegistry().getInterceptionModel(getType()) == null)
      {
         InterceptionModelBuilder<Class<?>, SerializableContextual<Interceptor<?>, ?>> builder =
               InterceptionModelBuilder.newBuilderFor(getType(), (Class) SerializableContextual.class);
         Set<Annotation> classBindingAnnotations = flattenInterceptorBindings(manager, getAnnotatedItem().getAnnotations());
         for (Class<? extends Annotation> annotation : getStereotypes())
         {
            classBindingAnnotations.addAll(flattenInterceptorBindings(manager, manager.getStereotypeDefinition(annotation)));
         }
         if (classBindingAnnotations.size() > 0)
         {
            if (Beans.findInterceptorBindingConflicts(manager, classBindingAnnotations))
               throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());

            Annotation[] classBindingAnnotationsArray = classBindingAnnotations.toArray(new Annotation[0]);

            List<Interceptor<?>> resolvedPostConstructInterceptors = manager.resolveInterceptors(InterceptionType.POST_CONSTRUCT, classBindingAnnotationsArray);
            builder.interceptPostConstruct().with(toSerializableContextualArray(resolvedPostConstructInterceptors));

            List<Interceptor<?>> resolvedPreDestroyInterceptors = manager.resolveInterceptors(InterceptionType.PRE_DESTROY, classBindingAnnotationsArray);
            builder.interceptPreDestroy().with(toSerializableContextualArray(resolvedPreDestroyInterceptors));

            List<Interceptor<?>> resolvedPrePassivateInterceptors = manager.resolveInterceptors(InterceptionType.PRE_PASSIVATE, classBindingAnnotationsArray);
            builder.interceptPrePassivate().with(toSerializableContextualArray(resolvedPrePassivateInterceptors));

            List<Interceptor<?>> resolvedPostActivateInterceptors = manager.resolveInterceptors(InterceptionType.POST_ACTIVATE, classBindingAnnotationsArray);
            builder.interceptPostActivate().with(toSerializableContextualArray(resolvedPostActivateInterceptors));

         }
         List<WeldMethod<?, ?>> businessMethods = Beans.getInterceptableMethods(getAnnotatedItem());
         for (WeldMethod<?, ?> method : businessMethods)
         {
            Set<Annotation> methodBindingAnnotations = new HashSet<Annotation>(classBindingAnnotations);
            methodBindingAnnotations.addAll(flattenInterceptorBindings(manager, method.getAnnotations()));
            if (methodBindingAnnotations.size() > 0)
            {
               if (Beans.findInterceptorBindingConflicts(manager, classBindingAnnotations))
                  throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType() + "." + method.getName() + "()");

               if (method.isAnnotationPresent(manager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS))
               {
                  List<Interceptor<?>> methodBoundInterceptors = manager.resolveInterceptors(InterceptionType.AROUND_TIMEOUT, methodBindingAnnotations.toArray(new Annotation[]{}));
                  builder.interceptAroundTimeout(((AnnotatedMethod) method).getJavaMember()).with(toSerializableContextualArray(methodBoundInterceptors));
               }
               else
               {
                  List<Interceptor<?>> methodBoundInterceptors = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, methodBindingAnnotations.toArray(new Annotation[]{}));
                  builder.interceptAroundInvoke(((AnnotatedMethod) method).getJavaMember()).with(toSerializableContextualArray(methodBoundInterceptors));
               }
            }
         }
         InterceptionModel<Class<?>,SerializableContextual<Interceptor<?>,?>> serializableContextualInterceptionModel = builder.build();
         // if there is at least one applicable interceptor, register it 
         if (serializableContextualInterceptionModel.getAllInterceptors().size() > 0)
         {
            manager.getCdiInterceptorsRegistry().registerInterceptionModel(getType(), serializableContextualInterceptionModel);
         }
      }
   }
   
   public void setInjectionTarget(InjectionTarget<T> injectionTarget)
   {
      this.injectionTarget = injectionTarget;
   }
   
   public InjectionTarget<T> getInjectionTarget()
   {
      return injectionTarget;
   }
   
   @Override
   public Set<InjectionPoint> getInjectionPoints()
   {
      return getInjectionTarget().getInjectionPoints();
   }
   
   protected void defaultPreDestroy(T instance)
   {
       WeldMethod<?, ?> preDestroy = getPreDestroy();
       if (preDestroy != null)
       {
          try
          {
             // note: RI supports injection into @PreDestroy
             preDestroy.invoke(instance);
          }
          catch (Exception e)
          {
             throw new WeldException(INVOCATION_ERROR, e, preDestroy, instance);
          }
       }
   }
   
   protected void defaultPostConstruct(T instance)
   {
       WeldMethod<?, ?> postConstruct = getPostConstruct();
       if (postConstruct != null)
       {
          try
          {
             postConstruct.invoke(instance);
          }
          catch (Exception e)
          {
             throw new WeldException(INVOCATION_ERROR, e, postConstruct, instance);
          }
       }
   }

   private static SerializableContextual[] toSerializableContextualArray(List<Interceptor<?>> interceptors)
   {
      List<SerializableContextual> serializableContextuals = new ArrayList<SerializableContextual>();
      for (Interceptor<?> interceptor: interceptors)
      {
         serializableContextuals.add(new SerializableContextualImpl(interceptor));
      }
      return serializableContextuals.toArray(new SerializableContextual[]{});
   }

   public boolean hasCdiBoundInterceptors()
   {
      if (manager.getCdiInterceptorsRegistry().getInterceptionModel(getType()) != null)
         return manager.getCdiInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors().size() > 0;
      else
         return false;
   }

   public boolean hasDirectlyDefinedInterceptors()
   {
      if (manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()) != null)
      {
         return hasSerializationOrInvocationInterceptorMethods || manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors().size() > 0;
      }
      else
      {
         return false;
      }
   }

   protected void initDirectlyDefinedInterceptors()
   {
      if (manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()) == null && InterceptionUtils.supportsEjb3InterceptorDeclaration())
      {
         InterceptionModelBuilder<Class<?>, Class<?>> builder = InterceptionModelBuilder.newBuilderFor(getType(), (Class) Class.class);

         Class<?>[] classDeclaredInterceptors = null;
         if (getAnnotatedItem().isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass()))
         {
            Annotation interceptorsAnnotation = getType().getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass());
            classDeclaredInterceptors = SecureReflections.extractValues(interceptorsAnnotation);
         }

         if (classDeclaredInterceptors != null)
         {
            builder.interceptAll().with(classDeclaredInterceptors);
         }

         List<WeldMethod<?, ?>> businessMethods = Beans.getInterceptableMethods(getAnnotatedItem());
         for (WeldMethod<?, ?> method : businessMethods)
         {
            boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
            Class<?>[] methodDeclaredInterceptors = null;
            if (method.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass()))
            {
               methodDeclaredInterceptors = SecureReflections.extractValues(method.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass()));
            }
            if (excludeClassInterceptors)
            {
               builder.ignoreGlobalInterceptors(((AnnotatedMethod)method).getJavaMember());
            }
            if (methodDeclaredInterceptors != null)
            {
               if (method.isAnnotationPresent(manager.getServices().get(EJBApiAbstraction.class).TIMEOUT_ANNOTATION_CLASS))
                  builder.interceptAroundTimeout(((AnnotatedMethod) method).getJavaMember()).with(methodDeclaredInterceptors);
               else
                  builder.interceptAroundInvoke(((AnnotatedMethod) method).getJavaMember()).with(methodDeclaredInterceptors);
            }
         }
         InterceptionModel<Class<?>, Class<?>> interceptionModel = builder.build();
         InterceptorClassMetadataImpl interceptorClassMetadata = new InterceptorClassMetadataImpl(getType());
         hasSerializationOrInvocationInterceptorMethods =
               !interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.AROUND_INVOKE).isEmpty()
               || !interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.AROUND_TIMEOUT).isEmpty()
               || !interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.PRE_PASSIVATE).isEmpty()
               || !interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.POST_ACTIVATE).isEmpty();
         if (interceptionModel.getAllInterceptors().size() > 0 || hasSerializationOrInvocationInterceptorMethods)
            manager.getClassDeclaredInterceptorsRegistry().registerInterceptionModel(getType(), builder.build());
      }
   }

   protected void checkConstructor()
   {
      if (!constructor.getAnnotatedWBParameters(Disposes.class).isEmpty())
      {
         throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Disposes", constructor);
      }
      if (!constructor.getAnnotatedWBParameters(Observes.class).isEmpty())
      {
         throw new DefinitionException(PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR, "@Observes", constructor);
      }
   }

   /**
    * Initializes the constructor
    */
   protected void initConstructor()
   {
      this.constructor = Beans.getBeanConstructor(this, getAnnotatedItem());
      // TODO We loop unecessarily many times here, I want to probably introduce some callback mechanism. PLM.
      addInjectionPoints(Beans.getParameterInjectionPoints(this, constructor));
   }

   /**
    * Returns the constructor
    *
    * @return The constructor
    */
   public ConstructorInjectionPoint<T> getConstructor()
   {
      return constructor;
   }


}
