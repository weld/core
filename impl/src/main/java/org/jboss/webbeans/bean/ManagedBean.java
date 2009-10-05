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
package org.jboss.webbeans.bean;

import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.proxy.DirectClassInterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.interceptor.proxy.InterceptorProxyCreatorImpl;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.DeploymentException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.injection.ConstructorInjectionPoint;
import org.jboss.webbeans.injection.InjectionContextImpl;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBConstructor;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

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
   private static LogProvider log = Logging.getLogProvider(ManagedBean.class);

   // The constructor
   private ConstructorInjectionPoint<T> constructor;
   private Set<WBInjectionPoint<?, ?>> ejbInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> resourceInjectionPoints;

   private ManagedBean<?> specializedBean;


   /**
    * Creates a simple, annotation defined Web Bean
    *
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return A Web Bean
    */
   public static <T> ManagedBean<T> of(WBClass<T> clazz, BeanManagerImpl manager)
   {
      return new ManagedBean<T>(clazz, new StringBuilder().append(ManagedBean.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(clazz.getName()).toString(), manager);
   }

   /**
    * Constructor
    *
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   protected ManagedBean(WBClass<T> type, String idSuffix, BeanManagerImpl manager)
   {
      super(type, idSuffix, manager);
      initType();
      initTypes();
      initBindings();
   }

   /**
    * Creates an instance of the bean
    *
    * @return The instance
    */
   public T create(CreationalContext<T> creationalContext)
   {
      InjectionPoint originalInjectionPoint = null;
      if (hasDecorators())
      {
         originalInjectionPoint = attachCorrectInjectionPoint();
      }
      T instance = produce(creationalContext);
      inject(instance, creationalContext);
      if (hasDecorators())
      {
         instance = applyDecorators(instance, creationalContext, originalInjectionPoint);
      }
      if (isInterceptionCandidate() && hasInterceptors())
      {
         instance = applyInterceptors(instance, creationalContext);
         InterceptionUtils.executePostConstruct(instance);
      } else
      {
         postConstruct(instance);
      }
      return instance;
   }

   public T produce(CreationalContext<T> ctx)
   {
      T instance = constructor.newInstance(manager, ctx);
      if (!hasDecorators())
      {
         // This should be safe, but needs verification PLM
         // Without this, the chaining of decorators will fail as the incomplete instance will be resolved
         ctx.push(instance);
      }
      return instance;
   }

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

   protected InjectionPoint attachCorrectInjectionPoint()
   {
      Decorator<?> decorator = getDecorators().get(getDecorators().size() - 1);
      if (decorator instanceof DecoratorImpl<?>)
      {
         DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
         InjectionPoint outerDelegateInjectionPoint = decoratorBean.getDelegateInjectionPoint();
         return getManager().replaceOrPushCurrentInjectionPoint(outerDelegateInjectionPoint);
      } else
      {
         throw new IllegalStateException("Cannot operate on user defined decorator");
      }
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
         if (!isInterceptionCandidate() || !hasInterceptors())
            preDestroy(instance);
         else
         {
            InterceptionUtils.executePredestroy(instance);
         }
         creationalContext.release();
      }
      catch (Exception e)
      {
         log.error("Error destroying " + toString(), e);
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
         initConstructor();
         checkConstructor();
         super.initialize(environment);
         initPostConstruct();
         initPreDestroy();
         initEEInjectionPoints();
      }
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
   protected void checkType()
   {
      if (getAnnotatedItem().isNonStaticMemberClass())
      {
         throw new DefinitionException("Simple bean " + type + " cannot be a non-static inner class");
      }
      if (!isDependent() && getAnnotatedItem().isParameterizedType())
      {
         throw new DefinitionException("Managed bean " + type + " must be @Dependent");
      }
      boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(scopeType).isPassivating();
      if (passivating && !Reflections.isSerializable(getBeanClass()))
      {
         throw new DefinitionException("Simple bean declaring a passivating scope must have a serializable implementation class " + toString());
      }
      if (hasDecorators())
      {
         if (getAnnotatedItem().isFinal())
         {
            throw new DefinitionException("Bean class which has decorators cannot be declared final " + this);
         }
         for (Decorator<?> decorator : getDecorators())
         {
            if (decorator instanceof DecoratorImpl<?>)
            {
               DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
               for (WBMethod<?, ?> decoratorMethod : decoratorBean.getAnnotatedItem().getWBMethods())
               {
                  WBMethod<?, ?> method = getAnnotatedItem().getWBMethod(decoratorMethod.getSignature());
                  if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal())
                  {
                     throw new DefinitionException("Decorated bean method " + method + " (decorated by "+ decoratorMethod + ") cannot be declarted final");
                  }
               }
            }
            else
            {
               throw new IllegalStateException("Can only operate on container provided decorators " + decorator);
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
         for (WBField<?, ?> field : getAnnotatedItem().getWBFields())
         {
            if (field.isPublic() && !field.isStatic())
            {
               throw new DefinitionException("Normal scoped Web Bean implementation class has a public field " + getAnnotatedItem());
            }
         }
      }
   }

   protected void checkConstructor()
   {
      if (!constructor.getAnnotatedWBParameters(Disposes.class).isEmpty())
      {
         throw new DefinitionException("Managed bean constructor must not have a parameter annotated @Disposes " + constructor);
      }
      if (!constructor.getAnnotatedWBParameters(Observes.class).isEmpty())
      {
         throw new DefinitionException("Managed bean constructor must not have a parameter annotated @Observes " + constructor);
      }
   }

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      if (environment.getEjbDescriptors().contains(getAnnotatedItem().getWBSuperclass().getJavaClass()))
      {
         throw new DefinitionException("Simple bean must specialize a simple bean");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getWBSuperclass()) == null)
      {
         throw new DefinitionException(toString() + " does not specialize a bean");
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getWBSuperclass());
      if (!(specializedBean instanceof ManagedBean))
      {
         throw new DefinitionException(toString() + " doesn't have a simple bean as a superclass " + specializedBean);
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
   public WBConstructor<T> getConstructor()
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

   protected boolean isInterceptionCandidate()
   {
      return !Beans.isInterceptor(getAnnotatedItem()) && !Beans.isDecorator(getAnnotatedItem());
   }

   private boolean hasInterceptors()
   {
      return manager.getBoundInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors().size() > 0;
   }

   protected T applyInterceptors(T instance, final CreationalContext<T> creationalContext)
   {
      try
      {
         InterceptionHandlerFactory<Interceptor> factory = new InterceptionHandlerFactory<Interceptor>()
         {
            public InterceptionHandler createFor(final Interceptor interceptor)
            {
               final Object instance = getManager().getReference(interceptor, creationalContext);
               return new DirectClassInterceptionHandler<Interceptor>(instance, interceptor.getBeanClass());
            }
         };
         instance = new InterceptorProxyCreatorImpl<Interceptor>(manager.getBoundInterceptorsRegistry(), factory).createProxyFromInstance(instance, getType());

      } catch (Exception e)
      {
         throw new DeploymentException(e);
      }
      return instance;
   }

}
