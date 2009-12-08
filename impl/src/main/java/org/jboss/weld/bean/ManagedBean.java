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
import static org.jboss.weld.logging.messages.BeanMessage.BEAN_MUST_BE_DEPENDENT;
import static org.jboss.weld.logging.messages.BeanMessage.DELEGATE_INJECTION_POINT_NOT_FOUND;
import static org.jboss.weld.logging.messages.BeanMessage.ERROR_DESTROYING;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_DECORATED_BEAN_METHOD_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.NON_CONTAINER_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.PARAMETER_ANNOTATION_NOT_ALLOWED_ON_CONSTRUCTOR;
import static org.jboss.weld.logging.messages.BeanMessage.PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL;
import static org.jboss.weld.logging.messages.BeanMessage.PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.interceptor.proxy.InterceptorProxyCreatorImpl;
import org.jboss.interceptor.registry.InterceptorRegistry;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.DeploymentException;
import org.jboss.weld.ForbiddenStateException;
import org.jboss.weld.bean.interceptor.CdiInterceptorHandlerFactory;
import org.jboss.weld.bean.interceptor.ClassInterceptionHandlerFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.reflection.Reflections;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLogger.Level;

/**
 * Represents a simple bean
 *
 * @author Pete Muir
 * @author Marius Bogoevici
 * @param <T> The type (class) of the bean
 */
public class ManagedBean<T> extends AbstractClassBean<T>
{
   // Logger
   private static final LocLogger log = loggerFactory().getLogger(BEAN);
   private static final XLogger xLog = loggerFactory().getXLogger(BEAN);

   // The constructor
   private ConstructorInjectionPoint<T> constructor;
   
   // The Java EE style injection points
   private Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

   private ManagedBean<?> specializedBean;
   
   private boolean passivationCapableBean;
   private boolean passivationCapableDependency;

   /**
    * Creates a simple, annotation defined Web Bean
    *
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return A Web Bean
    */
   public static <T> ManagedBean<T> of(WeldClass<T> clazz, BeanManagerImpl manager)
   {
      return new ManagedBean<T>(clazz, createId(ManagedBean.class.getSimpleName(), clazz), manager);
   }
   
   protected static String createId(String beanType, WeldClass<?> clazz)
   {
      return new StringBuilder().append(beanType).append(BEAN_ID_SEPARATOR).append(clazz.getBaseType()).toString();
   }

   /**
    * Constructor
    *
    * @param type The type of the bean
    * @param manager The Bean manager
    */
   protected ManagedBean(WeldClass<T> type, String idSuffix, BeanManagerImpl manager)
   {
      super(type, idSuffix, manager);
      initType();
      initTypes();
      initBindings();
      initConstructor();
   }

   /**
    * Creates an instance of the bean
    *
    * @return The instance
    */
   public T create(CreationalContext<T> creationalContext)
   {
      T instance = getInjectionTarget().produce(creationalContext);
      getInjectionTarget().inject(instance, creationalContext);
      if (isInterceptionCandidate() && (hasCdiBoundInterceptors() || hasDirectlyDefinedInterceptors()))
      {
         InterceptionUtils.executePostConstruct(instance);
      }
      else
      {
         getInjectionTarget().postConstruct(instance);
      }
      return instance;
   }

   protected InjectionPoint attachCorrectInjectionPoint()
   {
      Decorator<?> decorator = getDecorators().get(getDecorators().size() - 1);
      InjectionPoint outerDelegateInjectionPoint = Beans.getDelegateInjectionPoint(decorator);
      if (outerDelegateInjectionPoint == null)
      {
         throw new ForbiddenStateException(DELEGATE_INJECTION_POINT_NOT_FOUND, decorator);
      }
      return getManager().replaceOrPushCurrentInjectionPoint(outerDelegateInjectionPoint);
   }

   /**
    * Destroys an instance of the bean
    *
    * @param instance The instance
    */
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      try
      {
         if (!isInterceptionCandidate() || !(hasCdiBoundInterceptors() || hasDirectlyDefinedInterceptors()))
         {
            getInjectionTarget().preDestroy(instance);
         }
         else
         {
            InterceptionUtils.executePredestroy(instance);
         }
         creationalContext.release();
      }
      catch (Exception e)
      {
         log.error(ERROR_DESTROYING, this, instance);
         xLog.throwing(Level.DEBUG, e);
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      if (!isInitialized())
      {
         checkConstructor();
         super.initialize(environment);
         initPostConstruct();
         initPreDestroy();
         initEEInjectionPoints();
         initPassivationCapable();
         if (isInterceptionCandidate())
         {
            initDirectlyDefinedInterceptors();
         }
         setInjectionTarget(new InjectionTarget<T>()
         {

            public void inject(final T instance, final CreationalContext<T> ctx)
            {
               new InjectionContextImpl<T>(getManager(), this, instance)
               {
                  
                  public void proceed()
                  {
                     Beans.injectEEFields(instance, getManager(), ejbInjectionPoints, persistenceContextInjectionPoints, persistenceUnitInjectionPoints, resourceInjectionPoints);
                     Beans.injectFieldsAndInitializers(instance, ctx, getManager(), getInjectableFields(), getInitializerMethods());
                  }

               }.run();
            }

            public void postConstruct(T instance)
            {
               defaultPostConstruct(instance);
            }

            public void preDestroy(T instance)
            {
               defaultPreDestroy(instance);
            }

            public void dispose(T instance)
            {
               // No-op
            }

            public Set<InjectionPoint> getInjectionPoints()
            {
               return (Set) getAnnotatedInjectionPoints();
            }

            public T produce(CreationalContext<T> ctx)
            {
               T instance = ManagedBean.this.createInstance(ctx);
               if (!hasDecorators())
               {
                  // This should be safe, but needs verification PLM
                  // Without this, the chaining of decorators will fail as the incomplete instance will be resolved
                  ctx.push(instance);
               }
               InjectionPoint originalInjectionPoint = null;
               if (hasDecorators())
               {
                  originalInjectionPoint = attachCorrectInjectionPoint();
               }
               if (hasDecorators())
               {
                  instance = applyDecorators(instance, ctx, originalInjectionPoint);
               }
               if (isInterceptionCandidate() && (hasCdiBoundInterceptors() || hasDirectlyDefinedInterceptors()))
               {
                  instance = applyInterceptors(instance, ctx);
               }
               return instance;
            }
         });
      }
   }

   protected T createInstance(CreationalContext<T> ctx) 
   {
      return constructor.newInstance(manager, ctx);
   }

   @Override
   public void initializeAfterBeanDiscovery()
   {
      super.initializeAfterBeanDiscovery();
      if (this.passivationCapableBean && this.hasDecorators())
      {
         for (Decorator<?> decorator : this.getDecorators())
         {
            if (!(PassivationCapable.class.isAssignableFrom(decorator.getClass())) || !Reflections.isSerializable(decorator.getBeanClass()))
            {
               this.passivationCapableBean = false;
               break;
            }
         }
      }
      if (this.passivationCapableBean && hasCdiBoundInterceptors())
      {
         for (SerializableContextual<Interceptor<?>, ?> interceptor : getManager().getCdiInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors())
         {
            if (!(PassivationCapable.class.isAssignableFrom(interceptor.get().getClass())) || !Reflections.isSerializable(interceptor.get().getBeanClass()))
            {
               this.passivationCapableBean = false;
               break;
            }
         }
      }
      if (this.passivationCapableBean && hasDirectlyDefinedInterceptors())
      {
         for (Class<?> interceptorClass : getManager().getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors())
         {
            if (!Reflections.isSerializable(interceptorClass))
            {
               this.passivationCapableBean = false;
               break;
            }
         }
      }
   }

   private void initPassivationCapable()
   {
      this.passivationCapableBean = Reflections.isSerializable(getAnnotatedItem().getJavaClass());
      if (Container.instance().deploymentServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isNormal())
      {
         this.passivationCapableDependency = true;
      }
      else if (getScope().equals(Dependent.class) && passivationCapableBean)
      {
         this.passivationCapableDependency = true;
      }
      else
      {
         this.passivationCapableDependency = false;
      }
   }
   
   @Override
   public boolean isPassivationCapableBean()
   {
      return passivationCapableBean;
   }
   
   @Override
   public boolean isPassivationCapableDependency()
   {
      return passivationCapableDependency;
   }

   private void initEEInjectionPoints()
   {
      this.ejbInjectionPoints = Beans.getEjbInjectionPoints(this, getAnnotatedItem(), getManager());
      this.persistenceContextInjectionPoints = Beans.getPersistenceContextInjectionPoints(this, getAnnotatedItem(), getManager());
      this.persistenceUnitInjectionPoints = Beans.getPersistenceUnitInjectionPoints(this, getAnnotatedItem(), getManager());
      this.resourceInjectionPoints = Beans.getResourceInjectionPoints(this, getAnnotatedItem(), manager);
   }




   /**
    * Validates the type
    */
   @Override
   public void checkType()
   {
      if (getAnnotatedItem().isAnonymousClass() || (getAnnotatedItem().isMemberClass() && !getAnnotatedItem().isStatic()))
      {
         throw new DefinitionException(SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED, type);
      }
      if (!isDependent() && getAnnotatedItem().isParameterizedType())
      {
         throw new DefinitionException(BEAN_MUST_BE_DEPENDENT, type);
      }
      boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(scopeType).isPassivating();
      if (passivating && !isPassivationCapableBean())
      {
         throw new DefinitionException(PASSIVATING_BEAN_NEEDS_SERIALIZABLE_IMPL, this);
      }
      if (hasDecorators())
      {
         if (getAnnotatedItem().isFinal())
         {
            throw new DefinitionException(FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED, this);
         }
         for (Decorator<?> decorator : getDecorators())
         {
            WeldClass<?> decoratorClass;
            if (decorator instanceof DecoratorImpl<?>)
            {
               DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
               decoratorClass = decoratorBean.getAnnotatedItem();
            }
            else if (decorator instanceof AnnotatedItemProvidingDecoratorWrapper)
            {
               decoratorClass = ((AnnotatedItemProvidingDecoratorWrapper) decorator).getAnnotatedItem();
            }
            else
            {
               throw new ForbiddenStateException(NON_CONTAINER_DECORATOR, decorator);
            }

            for (WeldMethod<?, ?> decoratorMethod : decoratorClass.getWeldMethods())
            {
               WeldMethod<?, ?> method = getAnnotatedItem().getWeldMethod(decoratorMethod.getSignature());
               if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal())
               {
                  throw new DefinitionException(FINAL_DECORATED_BEAN_METHOD_NOT_ALLOWED, method, decoratorMethod);
               }
            }
         }
      }
   }

   @Override
   protected void checkBeanImplementation()
   {
      super.checkBeanImplementation();
      if (isNormalScoped())
      {
         for (WeldField<?, ?> field : getAnnotatedItem().getWeldFields())
         {
            if (field.isPublic() && !field.isStatic())
            {
               throw new DefinitionException(PUBLIC_FIELD_ON_NORMAL_SCOPED_BEAN_NOT_ALLOWED, getAnnotatedItem());
            }
         }
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

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      if (environment.getEjbDescriptors().contains(getAnnotatedItem().getWeldSuperclass().getJavaClass()))
      {
         throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getWeldSuperclass()) == null)
      {
         throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getWeldSuperclass());
      if (!(specializedBean instanceof ManagedBean))
      {
         throw new DefinitionException(SPECIALIZING_BEAN_MUST_EXTEND_A_BEAN, this);
      } else
      {
         this.specializedBean = (ManagedBean<?>) specializedBean;
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

   /**
    * Gets a string representation
    *
    * @return The string representation
    */
   @Override
   public String getDescription()
   {
      return getDescription("simple bean");
   }

   protected String getDescription(String beanType)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Names.scopeTypeToString(getScope()));
      if (getName() == null)
      {
         buffer.append("unnamed ").append(beanType);
      }
      else
      {
         buffer.append(beanType).append(" '" + getName() + "'");
      }
      buffer.append(" ").append(getType().getName()).append(", ");
      buffer.append(" API types = ").append(Names.typesToString(getTypes())).append(", binding types = " + Names.annotationsToString(getQualifiers()));
      return buffer.toString();
   }

   @Override
   public ManagedBean<?> getSpecializedBean()
   {
      return specializedBean;
   }

   @Override
   protected boolean isInterceptionCandidate()
   {
      return !Beans.isInterceptor(getAnnotatedItem()) && !Beans.isDecorator(getAnnotatedItem());
   }

   protected T applyInterceptors(T instance, final CreationalContext<T> creationalContext)
   {
      try
      {
         List<InterceptorRegistry<Class<?>, ?>> interceptionRegistries = new ArrayList<InterceptorRegistry<Class<?>,?>>();
         List<InterceptionHandlerFactory<?>> interceptionHandlerFactories = new ArrayList<InterceptionHandlerFactory<?>>();
         if (hasDirectlyDefinedInterceptors())
         {
            interceptionRegistries.add(manager.getClassDeclaredInterceptorsRegistry());
            interceptionHandlerFactories.add(new ClassInterceptionHandlerFactory(creationalContext, getManager()));
         }
         if (hasCdiBoundInterceptors())
         {
            interceptionRegistries.add(manager.getCdiInterceptorsRegistry());
            interceptionHandlerFactories.add(new CdiInterceptorHandlerFactory(creationalContext, manager));
         }
         if (interceptionRegistries.size() > 0)
            instance = new InterceptorProxyCreatorImpl(interceptionRegistries, interceptionHandlerFactories).createProxyFromInstance(instance, getType());

      } catch (Exception e)
      {
         throw new DeploymentException(e);
      }
      return instance;
   }

}
