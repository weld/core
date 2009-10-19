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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;

import org.jboss.interceptor.model.InterceptionModel;
import org.jboss.interceptor.model.InterceptionModelBuilder;
import org.jboss.interceptor.model.InterceptorClassMetadataImpl;
import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.interceptor.proxy.InterceptorProxyCreatorImpl;
import org.jboss.interceptor.registry.InterceptorRegistry;
import org.jboss.interceptor.util.InterceptionUtils;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.DeploymentException;
import org.jboss.weld.bean.interceptor.CdiInterceptorHandlerFactory;
import org.jboss.weld.bean.interceptor.ClassInterceptionHandlerFactory;
import org.jboss.weld.bootstrap.BeanDeployerEnvironment;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldConstructor;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.log.LogProvider;
import org.jboss.weld.log.Logging;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.Reflections;

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
   
   // The Java EE style injection points
   private Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   private Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;

   private ManagedBean<?> specializedBean;

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
         if (!isInterceptionCandidate() || !hasCdiBoundInterceptors())
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
         checkConstructor();
         super.initialize(environment);
         initPostConstruct();
         initPreDestroy();
         initEEInjectionPoints();
         if (isInterceptionCandidate())
         {
            initClassInterceptors();
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
               T instance = constructor.newInstance(manager, ctx);
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
               for (WeldMethod<?, ?> decoratorMethod : decoratorBean.getAnnotatedItem().getWeldMethods())
               {
                  WeldMethod<?, ?> method = getAnnotatedItem().getWBMethod(decoratorMethod.getSignature());
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
         for (WeldField<?, ?> field : getAnnotatedItem().getWeldFields())
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
      if (environment.getEjbDescriptors().contains(getAnnotatedItem().getWeldSuperclass().getJavaClass()))
      {
         throw new DefinitionException("Simple bean must specialize a simple bean");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getWeldSuperclass()) == null)
      {
         throw new DefinitionException(toString() + " does not specialize a bean");
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getWeldSuperclass());
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
   public WeldConstructor<T> getConstructor()
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

   public boolean hasDirectlyDefinedInterceptors()
   {
      if (manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()) != null)
         return manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()).getAllInterceptors().size() > 0;
      else
         return false;
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

   protected void initClassInterceptors()
   {
      if (manager.getClassDeclaredInterceptorsRegistry().getInterceptionModel(getType()) == null && InterceptionUtils.supportsEjb3InterceptorDeclaration())
      {
         InterceptionModelBuilder<Class<?>, Class<?>> builder = InterceptionModelBuilder.newBuilderFor(getType(), (Class) Class.class);

         Class<?>[] classDeclaredInterceptors = null;
         if (getAnnotatedItem().isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass()))
         {
            Annotation interceptorsAnnotation = getType().getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass());
            classDeclaredInterceptors = Reflections.extractValues(interceptorsAnnotation);
         }

         if (classDeclaredInterceptors != null)
         {
            builder.interceptPostConstruct().with(classDeclaredInterceptors);
            builder.interceptPreDestroy().with(classDeclaredInterceptors);
            builder.interceptPrePassivate().with(classDeclaredInterceptors);
            builder.interceptPostActivate().with(classDeclaredInterceptors);
         }

         List<WeldMethod<?, ?>> businessMethods = Beans.getInterceptableBusinessMethods(getAnnotatedItem());
         for (WeldMethod<?, ?> method : businessMethods)
         {
            boolean excludeClassInterceptors = method.isAnnotationPresent(InterceptionUtils.getExcludeClassInterceptorsAnnotationClass());
            Class<?>[] methodDeclaredInterceptors = null;
            if (method.isAnnotationPresent(InterceptionUtils.getInterceptorsAnnotationClass()))
            {
               methodDeclaredInterceptors = Reflections.extractValues(method.getAnnotation(InterceptionUtils.getInterceptorsAnnotationClass()));
            }
            if (!excludeClassInterceptors && classDeclaredInterceptors != null)
            {
               builder.interceptAroundInvoke(((AnnotatedMethod) method).getJavaMember()).with(classDeclaredInterceptors);
            }
            if (methodDeclaredInterceptors != null)
            {
               builder.interceptAroundInvoke(((AnnotatedMethod) method).getJavaMember()).with(methodDeclaredInterceptors);
            }
         }
         InterceptionModel<Class<?>, Class<?>> interceptionModel = builder.build();
         if (interceptionModel.getAllInterceptors().size() > 0 || new InterceptorClassMetadataImpl(getType()).isInterceptor())
            manager.getClassDeclaredInterceptorsRegistry().registerInterceptionModel(getType(), builder.build());
      }
   }

}
