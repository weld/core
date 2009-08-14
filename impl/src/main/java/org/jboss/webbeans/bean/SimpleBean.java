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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.injection.ConstructorInjectionPoint;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBConstructor;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

/**
 * Represents a simple bean
 * 
 * @author Pete Muir
 * 
 * @param <T> The type (class) of the bean
 */
public class SimpleBean<T> extends AbstractClassBean<T>
{
   // Logger
   private static LogProvider log = Logging.getLogProvider(SimpleBean.class);

   // The constructor
   private ConstructorInjectionPoint<T> constructor;
   // The post-construct method
   private WBMethod<?, ?> postConstruct;
   // The pre-destroy method
   private WBMethod<?, ?> preDestroy;

   private Set<WBInjectionPoint<?, ?>> ejbInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> persistenceContextInjectionPoints;
   private HashSet<WBInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> resourceInjectionPoints;

   private SimpleBean<?> specializedBean;

   

   /**
    * Creates a simple, annotation defined Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return A Web Bean
    */
   public static <T> SimpleBean<T> of(WBClass<T> clazz, BeanManagerImpl manager)
   {
      return new SimpleBean<T>(clazz, manager);
   }

   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   protected SimpleBean(WBClass<T> type, BeanManagerImpl manager)
   {
      super(type, manager);
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
      postConstruct(instance);
      if (hasDecorators())
      {
         instance = applyDecorators(instance, creationalContext, originalInjectionPoint);
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
   
   public void inject(T instance, CreationalContext<T> ctx)
   {
      injectEjbAndCommonFields(instance);
      injectBoundFields(instance, ctx);
      callInitializers(instance, ctx);
   }

   public void postConstruct(T instance)
   {
      WBMethod<?, ?> postConstruct = getPostConstruct();
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

   public void preDestroy(T instance)
   {
      WBMethod<?, ?> preDestroy = getPreDestroy();
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

   
   
   protected InjectionPoint attachCorrectInjectionPoint()
   {
      Decorator<?> decorator = getDecorators().get(getDecorators().size() - 1);
      if (decorator instanceof DecoratorBean<?>)
      {
         DecoratorBean<?> decoratorBean = (DecoratorBean<?>) decorator;
         InjectionPoint outerDelegateInjectionPoint = decoratorBean.getDelegateInjectionPoint();
         return getManager().replaceOrPushCurrentInjectionPoint(outerDelegateInjectionPoint);
      }
      else
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
         preDestroy(instance);
         creationalContext.release();
      }
      catch (Exception e)
      {
         log.error("Error destroying " + toString(), e);
      }
   }

   protected void initEjbInjectionPoints()
   {
      Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
      this.ejbInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      for (WBField<?, ?> field : annotatedItem.getAnnotatedWBFields(ejbAnnotationType))
      {
         this.ejbInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }
   }

   protected void initJpaInjectionPoints()
   {
      this.persistenceContextInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      this.persistenceUnitInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      
      Class<? extends Annotation> persistenceContextAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
      for (WBField<?, ?> field : annotatedItem.getAnnotatedWBFields(persistenceContextAnnotationType))
      {
         this.persistenceContextInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }
      
      Class<? extends Annotation> persistenceUnitAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_UNIT_ANNOTATION_CLASS;
      for (WBField<?, ?> field : annotatedItem.getAnnotatedWBFields(persistenceUnitAnnotationType))
      {
         this.persistenceUnitInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }
   }

   protected void initResourceInjectionPoints()
   {
      Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
      this.resourceInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      for (WBField<?, ?> field : annotatedItem.getAnnotatedWBFields(resourceAnnotationType))
      {
         this.resourceInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }
   }

   /**
    * Injects EJBs and common fields
    */
   protected void injectEjbAndCommonFields(T beanInstance)
   {
      EjbServices ejbServices = manager.getServices().get(EjbServices.class);
      JpaServices jpaServices = manager.getServices().get(JpaServices.class);
      ResourceServices resourceServices = manager.getServices().get(ResourceServices.class);
      
      if (ejbServices != null)
      {
         for (WBInjectionPoint<?, ?> injectionPoint : ejbInjectionPoints)
         {
            Object ejbInstance = ejbServices.resolveEjb(injectionPoint);
            injectionPoint.inject(beanInstance, ejbInstance);
         }
      }

      if (jpaServices != null)
      {
         for (WBInjectionPoint<?, ?> injectionPoint : persistenceContextInjectionPoints)
         {
            Object pcInstance = jpaServices.resolvePersistenceContext(injectionPoint);
            injectionPoint.inject(beanInstance, pcInstance);
         }
         for (WBInjectionPoint<?, ?> injectionPoint : persistenceUnitInjectionPoints)
         {
            Object puInstance = jpaServices.resolvePersistenceUnit(injectionPoint);
            injectionPoint.inject(beanInstance, puInstance);
         }
      }

      if (resourceServices != null)
      {
         for (WBInjectionPoint<?, ?> injectionPoint : resourceInjectionPoints)
         {
            Object resourceInstance = resourceServices.resolveResource(injectionPoint);
            injectionPoint.inject(beanInstance, resourceInstance);
         }
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
         if (getManager().getServices().contains(EjbServices.class))
         {
            initEjbInjectionPoints();
         }
         if (getManager().getServices().contains(JpaServices.class))
         {
            initJpaInjectionPoints();
         }
         if (getManager().getServices().contains(ResourceServices.class))
         {
            initResourceInjectionPoints();
         }
      }
   }

   /**
    * Validates the type
    */
   protected void checkType()
   {
      if (getAnnotatedItem().isNonStaticMemberClass())
      {
         throw new DefinitionException("Simple bean " + type + " cannot be a non-static inner class");
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
            if (decorator instanceof DecoratorBean<?>)
            {
               DecoratorBean<?> decoratorBean = (DecoratorBean<?>) decorator;
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
      if (!isDependent())
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
      if (environment.getEjbDescriptors().containsKey(getAnnotatedItem().getWBSuperclass().getJavaClass()))
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
      if (!(specializedBean instanceof SimpleBean))
      {
         throw new DefinitionException(toString() + " doesn't have a simple bean as a superclass " + specializedBean);
      }
      else
      {
         this.specializedBean = (SimpleBean<?>) specializedBean;
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
    * Returns the constructor
    * 
    * @return The constructor
    */
   public WBConstructor<T> getConstructor()
   {
      return constructor;
   }

   /**
    * Returns the post-construct method
    * 
    * @return The post-construct method
    */
   public WBMethod<?, ?> getPostConstruct()
   {
      return postConstruct;
   }

   /**
    * Returns the pre-destroy method
    * 
    * @return The pre-destroy method
    */
   public WBMethod<?, ?> getPreDestroy()
   {
      return preDestroy;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return toString("simple bean");
   }
   
   protected String toString(String beanType)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append("unnamed ").append(beanType);
      }
      else
      {
         buffer.append(beanType).append(" '" + getName() + "'");
      }
      buffer.append(" ").append(getType().getName()).append(", ");
      buffer.append(" API types = ").append(Names.typesToString(getTypes())).append(", binding types = " + Names.annotationsToString(getBindings()));
      return buffer.toString();
   }

   @Override
   public SimpleBean<?> getSpecializedBean()
   {
      return specializedBean;
   }

}
