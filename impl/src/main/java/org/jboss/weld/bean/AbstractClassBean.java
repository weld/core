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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Scope;

import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.DeploymentException;
import org.jboss.weld.bean.proxy.DecoratorProxyMethodHandler;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.context.SerializableContextualInstance;
import org.jboss.weld.context.SerializableContextual;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.log.LogProvider;
import org.jboss.weld.log.Logging;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Strings;
import org.jboss.weld.util.Reflections;

/**
 * An abstract bean representation common for class-based beans
 * 
 * @author Pete Muir
 * 
 * @param <T>
 * @param <E>
 */
public abstract class AbstractClassBean<T> extends AbstractBean<T, Class<T>>
{
   // Logger
   private static final LogProvider log = Logging.getLogProvider(AbstractClassBean.class);
   // The item representation
   protected WeldClass<T> annotatedItem;
   // The injectable fields of each type in the type hierarchy, with the actual type at the bottom 
   private List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
   // The initializer methods of each type in the type hierarchy, with the actual type at the bottom
   private List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;
   private Set<String> dependencies;
   
   private List<Decorator<?>> decorators;
   
   private Class<T> proxyClassForDecorators;
   
   private final ThreadLocal<Integer> decoratorStackPosition;
   private WeldMethod<?, ?> postConstruct;
   private WeldMethod<?, ?> preDestroy;
   
   private InjectionTarget<T> injectionTarget;

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
      initPolicy();
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      initInitializerMethods();
      initInjectableFields();
      super.initialize(environment);
      checkBeanImplementation();
      initDecorators();
      checkType();
      initProxyClassForDecoratedBean();
      if (isInterceptionCandidate())
            initInterceptors();
   }
   
   protected void checkType()
   {
      
   }
   
   protected void initDecorators()
   {
      this.decorators = getManager().resolveDecorators(getTypes(), getQualifiers());
   }
   
   public boolean hasDecorators()
   {
      return this.decorators != null && this.decorators.size() > 0;
   }
   
   protected void initProxyClassForDecoratedBean()
   {
      if (hasDecorators())
      {
         Set<Type> types = new LinkedHashSet<Type>(getTypes());
         ProxyFactory proxyFactory = Proxies.getProxyFactory(types);
   
         @SuppressWarnings("unchecked")
         Class<T> proxyClass = proxyFactory.createClass();
   
         this.proxyClassForDecorators = proxyClass;
      }
   }
   
   protected T applyDecorators(T instance, CreationalContext<T> creationalContext, InjectionPoint originalInjectionPoint)
   {
      List<SerializableContextualInstance<DecoratorImpl<Object>, Object>> decoratorInstances = new ArrayList<SerializableContextualInstance<DecoratorImpl<Object>,Object>>();
      InjectionPoint ip = originalInjectionPoint;
      boolean outside = decoratorStackPosition.get().intValue() == 0;
      try
      {
         int i = decoratorStackPosition.get();
         while (i < decorators.size())
         {
            Decorator<?> decorator = decorators.get(i);
            if (decorator instanceof DecoratorImpl<?>)
            {
               decoratorStackPosition.set(++i);
               
               @SuppressWarnings("unchecked")
               DecoratorImpl<Object> decoratorBean = (DecoratorImpl<Object>) decorator;
               
               Object decoratorInstance = getManager().getReference(ip, decorator, creationalContext);
               decoratorInstances.add(new SerializableContextualInstance<DecoratorImpl<Object>, Object>(decoratorBean, decoratorInstance, null));
               ip = decoratorBean.getDelegateInjectionPoint();
            }
            else
            {
               throw new IllegalStateException("Cannot operate on non container provided decorator " + decorator);
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
         T proxy = proxyClassForDecorators.newInstance();
         ((ProxyObject) proxy).setHandler(new DecoratorProxyMethodHandler(decoratorInstances, instance));
         return proxy;
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException("Could not instantiate decorator proxy for " + toString(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Could not access bean correctly when creating decorator proxy for " + toString(), e);
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
      log.trace("Bean type specified in Java");
      this.type = getAnnotatedItem().getJavaClass();
      this.dependencies = new HashSet<String>();
      for (Class<?> clazz = type.getSuperclass(); clazz != Object.class; clazz = clazz.getSuperclass())
      {
         dependencies.add(clazz.getName());
      }
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
               log.trace("Scope " + scopeType + " specified by annotation");
            }
            break;
         }
         else if (scopeTypes.size() > 1)
         {
            throw new DefinitionException("At most one scope may be specified on " + getAnnotatedItem());
         }
      }

      if (this.scopeType == null)
      {
         initScopeTypeFromStereotype();
      }

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace("Using default @Dependent scope");
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
         throw new DefinitionException("Specializing bean must extend another bean " + toString());
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
      log.trace("Default name of " + type + " is " + name);
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
   
   // TODO maybe a better way to expose this?
   public Set<String> getSuperclasses()
   {
      return dependencies;
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
         if (manager.isInterceptorBindingType(annotation.annotationType()))
         {
            foundInterceptionBindingTypes.add(annotation);
            foundInterceptionBindingTypes.addAll(manager.getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotation.annotationType()).getInheritedInterceptionBindingTypes());
         }
      }
      return foundInterceptionBindingTypes;
   }



   protected void initInterceptors()
   {
      if (manager.getBoundInterceptorsRegistry().getInterceptionModel(getType()) == null)
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
               throw new DeploymentException("Conflicting interceptor bindings found on " + getType());

            Annotation[] classBindingAnnotationsArray = classBindingAnnotations.toArray(new Annotation[0]);

            List<Interceptor<?>> resolvedPostConstructInterceptors = manager.resolveInterceptors(InterceptionType.POST_CONSTRUCT, classBindingAnnotationsArray);
            validateSerializableInterceptors(resolvedPostConstructInterceptors);
            builder.interceptPostConstruct().with(toSerializableContextualArray(resolvedPostConstructInterceptors));

            List<Interceptor<?>> resolvedPreDestroyInterceptors = manager.resolveInterceptors(InterceptionType.PRE_DESTROY, classBindingAnnotationsArray);
            validateSerializableInterceptors(resolvedPreDestroyInterceptors);
            builder.interceptPreDestroy().with(toSerializableContextualArray(resolvedPreDestroyInterceptors));


            List<Interceptor<?>> resolvedPrePassivateInterceptors = manager.resolveInterceptors(InterceptionType.PRE_PASSIVATE, classBindingAnnotationsArray);
            validateSerializableInterceptors(resolvedPrePassivateInterceptors);
            builder.interceptPrePassivate().with(toSerializableContextualArray(resolvedPrePassivateInterceptors));

            List<Interceptor<?>> resolvedPostActivateInterceptors = manager.resolveInterceptors(InterceptionType.POST_ACTIVATE, classBindingAnnotationsArray);
            validateSerializableInterceptors(resolvedPostActivateInterceptors);
            builder.interceptPostActivate().with(toSerializableContextualArray(resolvedPostActivateInterceptors));

         }
         List<WeldMethod<?, ?>> businessMethods = Beans.getInterceptableBusinessMethods(getAnnotatedItem());
         for (WeldMethod<?, ?> method : businessMethods)
         {
            Set<Annotation> methodBindingAnnotations = new HashSet<Annotation>(classBindingAnnotations);
            methodBindingAnnotations.addAll(flattenInterceptorBindings(manager, method.getAnnotations()));
            if (methodBindingAnnotations.size() > 0)
            {
               if (Beans.findInterceptorBindingConflicts(manager, classBindingAnnotations))
                  throw new DeploymentException("Conflicting interceptor bindings found on " + getType() + "." + method.getName() + "()");
               List<Interceptor<?>> methodBoundInterceptors = manager.resolveInterceptors(InterceptionType.AROUND_INVOKE, methodBindingAnnotations.toArray(new Annotation[]{}));
               validateSerializableInterceptors(methodBoundInterceptors);
               builder.interceptAroundInvoke(((AnnotatedMethod) method).getJavaMember()).with(toSerializableContextualArray(methodBoundInterceptors));
            }
         }
         manager.getBoundInterceptorsRegistry().registerInterceptionModel(getType(), builder.build());
      }
   }

   private void validateSerializableInterceptors(Collection<Interceptor<?>> interceptors)
   {
      if (Beans.isPassivationCapableBean(this))
      {
         for (Interceptor<?> interceptor: interceptors)
         {
            if (!Reflections.isSerializable(interceptor.getBeanClass()))
            {
               throw new DeploymentException("The bean " + this + " declared a passivating scope, " +
                     "but has a non-serializable interceptor: "  + interceptor);
            }
            for (InjectionPoint injectionPoint: interceptor.getInjectionPoints())
            {
               Bean<?> resolvedBean = manager.resolve(manager.getInjectableBeans(injectionPoint));
               Beans.validateInjectionPointPassivationCapable(injectionPoint, resolvedBean, manager);
            }
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
             throw new RuntimeException("Unable to invoke " + preDestroy + " on " + instance, e);
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
             throw new RuntimeException("Unable to invoke " + postConstruct + " on " + instance, e);
          }
       }
   }

   private static SerializableContextual[] toSerializableContextualArray(List<Interceptor<?>> interceptors)
   {
      List<SerializableContextual> serializableContextuals = new ArrayList<SerializableContextual>();
      for (Interceptor<?> interceptor: interceptors)
      {
         serializableContextuals.add(new SerializableContextual(interceptor));
      }
      return serializableContextuals.toArray(new SerializableContextual[]{});
   }

}
