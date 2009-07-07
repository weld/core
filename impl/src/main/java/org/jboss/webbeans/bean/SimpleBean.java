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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.injection.ConstructorInjectionPoint;
import org.jboss.webbeans.injection.FieldInjectionPoint;
import org.jboss.webbeans.injection.MethodInjectionPoint;
import org.jboss.webbeans.injection.ParameterInjectionPoint;
import org.jboss.webbeans.injection.WBInjectionPoint;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBConstructor;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.introspector.WBParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceServices;
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
   private WBMethod<?> postConstruct;
   // The pre-destroy method
   private WBMethod<?> preDestroy;

   private Set<WBInjectionPoint<?, ?>> ejbInjectionPoints;
   private Set<WBInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
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
      T instance = null;
      instance = constructor.newInstance(manager, creationalContext);
      instance = applyDecorators(instance, creationalContext);
      creationalContext.push(instance);
      injectEjbAndCommonFields(instance);
      injectBoundFields(instance, creationalContext);
      callInitializers(instance, creationalContext);
      callPostConstruct(instance);
      return instance;
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
         callPreDestroy(instance);
         creationalContext.release();
      }
      catch (Exception e)
      {
         log.error("Error destroying " + toString(), e);
      }
   }

   /**
    * Calls the pre-destroy method, if any
    * 
    * @param instance The instance to invoke the method on
    */
   protected void callPreDestroy(T instance)
   {
      WBMethod<?> preDestroy = getPreDestroy();
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

   /**
    * Calls the post-construct method, if any
    * 
    * @param instance The instance to invoke the method on
    */
   protected void callPostConstruct(T instance)
   {
      WBMethod<?> postConstruct = getPostConstruct();
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

   protected void initEjbInjectionPoints()
   {
      Class<? extends Annotation> ejbAnnotationType = manager.getServices().get(EJBApiAbstraction.class).EJB_ANNOTATION_CLASS;
      this.ejbInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      for (WBField<?> field : annotatedItem.getAnnotatedFields(ejbAnnotationType))
      {
         this.ejbInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }

      for (WBMethod<?> method : annotatedItem.getAnnotatedMethods(ejbAnnotationType))
      {
         this.ejbInjectionPoints.add(MethodInjectionPoint.of(this, method));
      }
   }

   protected void initPersistenceUnitInjectionPoints()
   {
      this.persistenceUnitInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      Class<? extends Annotation> persistenceContextAnnotationType = manager.getServices().get(PersistenceApiAbstraction.class).PERSISTENCE_CONTEXT_ANNOTATION_CLASS;
      Object extendedPersistenceContextEnum = manager.getServices().get(PersistenceApiAbstraction.class).EXTENDED_PERSISTENCE_CONTEXT_ENUM_VALUE;
      
      for (WBField<?> field : annotatedItem.getAnnotatedFields(persistenceContextAnnotationType))
      {
         if (extendedPersistenceContextEnum.equals(Reflections.invokeAndWrap("type", field.getAnnotation(persistenceContextAnnotationType))))
         {
            throw new DefinitionException("Cannot inject an extended persistence context into " + field);
         }
         this.persistenceUnitInjectionPoints.add(FieldInjectionPoint.of(this, field));
      }

      for (WBMethod<?> method : annotatedItem.getAnnotatedMethods(persistenceContextAnnotationType))
      {
         if (extendedPersistenceContextEnum.equals(Reflections.invokeAndWrap("type", method.getAnnotation(persistenceContextAnnotationType))))
         {
            throw new DefinitionException("Cannot inject an extended persistence context into " + method);
         }
         this.persistenceUnitInjectionPoints.add(MethodInjectionPoint.of(this, method));
      }
   }

   protected void initResourceInjectionPoints()
   {
      Class<? extends Annotation> resourceAnnotationType = manager.getServices().get(EJBApiAbstraction.class).RESOURCE_ANNOTATION_CLASS;
      this.resourceInjectionPoints = new HashSet<WBInjectionPoint<?, ?>>();
      for (WBField<?> field : annotatedItem.getAnnotatedFields(resourceAnnotationType))
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
         for (WBInjectionPoint<?, ?> injectionPoint : persistenceUnitInjectionPoints)
         {
            Object puInstance = jpaServices.resolvePersistenceContext(injectionPoint);
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
         super.initialize(environment);
         initPostConstruct();
         initPreDestroy();
         if (getManager().getServices().contains(EjbServices.class))
         {
            initEjbInjectionPoints();
         }
         if (getManager().getServices().contains(JpaServices.class))
         {
            initPersistenceUnitInjectionPoints();
         }
         if (getManager().getServices().contains(ResourceServices.class))
         {
            initResourceInjectionPoints();
         }
      }
   }

   /**
    * Initializes the injection points
    */
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (WBParameter<?> parameter : constructor.getParameters())
      {
         injectionPoints.add(ParameterInjectionPoint.of(this, parameter));
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
      if (getAnnotatedItem().isParameterizedType())
      {
         throw new DefinitionException("Simple bean " + type + " cannot be a parameterized type");
      }
      boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(scopeType).isPassivating();
      if (passivating && !_serializable)
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
            if (decorator instanceof DecoratorBean)
            {
               DecoratorBean<?> decoratorBean = (DecoratorBean<?>) decorator;
               for (WBMethod<?> decoratorMethod : decoratorBean.getAnnotatedItem().getMethods())
               {
                  WBMethod<?> method = getAnnotatedItem().getMethod(decoratorMethod.getSignature());  
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
         for (WBField<?> field : getAnnotatedItem().getFields())
         {
            if (field.isPublic() && !field.isStatic())
            {
               throw new DefinitionException("Normal scoped Web Bean implementation class has a public field " + getAnnotatedItem());
            }
         }
      }
   }

   @Override
   protected void preSpecialize(BeanDeployerEnvironment environment)
   {
      super.preSpecialize(environment);
      if (environment.getEjbDescriptors().containsKey(getAnnotatedItem().getSuperclass().getJavaClass()))
      {
         throw new DefinitionException("Simple bean must specialize a simple bean");
      }
   }

   @Override
   protected void specialize(BeanDeployerEnvironment environment)
   {
      if (environment.getClassBean(getAnnotatedItem().getSuperclass()) == null)
      {
         throw new DefinitionException(toString() + " does not specialize a bean");
      }
      AbstractClassBean<?> specializedBean = environment.getClassBean(getAnnotatedItem().getSuperclass());
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
      Set<WBConstructor<T>> initializerAnnotatedConstructors = getAnnotatedItem().getAnnotatedConstructors(Initializer.class);
      log.trace("Found " + initializerAnnotatedConstructors + " constructors annotated with @Initializer for " + getType());
      if (initializerAnnotatedConstructors.size() > 1)
      {
         if (initializerAnnotatedConstructors.size() > 1)
         {
            throw new DefinitionException("Cannot have more than one constructor annotated with @Initializer for " + getType());
         }
      }
      else if (initializerAnnotatedConstructors.size() == 1)
      {
         this.constructor = ConstructorInjectionPoint.of(this, initializerAnnotatedConstructors.iterator().next());
         log.trace("Exactly one constructor (" + constructor + ") annotated with @Initializer defined, using it as the bean constructor for " + getType());
         return;
      }

      if (getAnnotatedItem().getNoArgsConstructor() != null)
      {

         this.constructor = ConstructorInjectionPoint.of(this, getAnnotatedItem().getNoArgsConstructor());
         log.trace("Exactly one constructor (" + constructor + ") defined, using it as the bean constructor for " + getType());
         return;
      }

      throw new DefinitionException("Cannot determine constructor to use for " + getType());
   }

   /**
    * Initializes the post-construct method
    */
   protected void initPostConstruct()
   {
      Set<WBMethod<?>> postConstructMethods = getAnnotatedItem().getAnnotatedMethods(PostConstruct.class);
      log.trace("Found " + postConstructMethods + " constructors annotated with @Initializer for " + getType());
      if (postConstructMethods.size() > 1)
      {
         // TODO actually this is wrong, in EJB you can have @PostConstruct
         // methods on the superclass, though the Web Beans spec is silent on
         // the issue
         throw new DefinitionException("Cannot have more than one post construct method annotated with @PostConstruct for " + getType());
      }
      else if (postConstructMethods.size() == 1)
      {
         this.postConstruct = postConstructMethods.iterator().next();
         log.trace("Exactly one post construct method (" + postConstruct + ") for " + getType());
         return;
      }
   }

   /**
    * Initializes the pre-destroy method
    */
   protected void initPreDestroy()
   {
      Set<WBMethod<?>> preDestroyMethods = getAnnotatedItem().getAnnotatedMethods(PreDestroy.class);
      log.trace("Found " + preDestroyMethods + " constructors annotated with @Initializer for " + getType());
      if (preDestroyMethods.size() > 1)
      {
         // TODO actually this is wrong, in EJB you can have @PreDestroy methods
         // on the superclass, though the Web Beans spec is silent on the issue
         throw new DefinitionException("Cannot have more than one pre destroy method annotated with @PreDestroy for " + getType());
      }
      else if (preDestroyMethods.size() == 1)
      {
         this.preDestroy = preDestroyMethods.iterator().next();
         log.trace("Exactly one post construct method (" + preDestroy + ") for " + getType());
         return;
      }
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
   public WBMethod<?> getPostConstruct()
   {
      return postConstruct;
   }

   /**
    * Returns the pre-destroy method
    * 
    * @return The pre-destroy method
    */
   public WBMethod<?> getPreDestroy()
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
      StringBuilder buffer = new StringBuilder();
      buffer.append(Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append("unnamed simple bean");
      }
      else
      {
         buffer.append("simple bean '" + getName() + "'");
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
