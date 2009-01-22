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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.ExecutionException;
import javax.webbeans.Initializer;
import javax.webbeans.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.injection.InjectionPointImpl;
import org.jboss.webbeans.injection.InjectionPointProvider;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedConstructor;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

/**
 * Represents a simple bean
 * 
 * @author Pete Muir
 * 
 * @param <T>
 */
public class SimpleBean<T> extends AbstractClassBean<T>
{
   // Logger
   private static LogProvider log = Logging.getLogProvider(SimpleBean.class);
   // Empty list representing no-args
   private static List<Class<?>> NO_ARGUMENTS = Collections.emptyList();
   // The constructor
   private AnnotatedConstructor<T> constructor;
   // The post-construct method
   private AnnotatedMethod<?> postConstruct;
   // The pre-destroy method
   private AnnotatedMethod<?> preDestroy;

   /**
    * Creates a simple, annotation defined Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return A Web Bean
    */
   public static <T> SimpleBean<T> of(Class<T> clazz, ManagerImpl manager)
   {
      return of(AnnotatedClassImpl.of(clazz), manager);
   }

   /**
    * Creates a simple, annotation defined Web Bean
    * 
    * @param <T> The type
    * @param clazz The class
    * @param manager the current manager
    * @return A Web Bean
    */
   public static <T> SimpleBean<T> of(AnnotatedClass<T> clazz, ManagerImpl manager)
   {
      return new SimpleBean<T>(clazz, manager);
   }

   /**
    * Constructor
    * 
    * @param type The type of the bean
    * @param manager The Web Beans manager
    */
   protected SimpleBean(AnnotatedClass<T> type, ManagerImpl manager)
   {
      super(type, manager);
      init();
   }

   /**
    * Creates an instance of the bean
    * 
    * @return The instance
    */
   @Override
   public T create()
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         InjectionPointProvider injectionPointProvider = manager.getInjectionPointProvider();
         injectionPointProvider.pushBean(this);
         T instance = null;
         try
         {
            instance = constructor.newInstance(manager);
            injectionPointProvider.setCurrentInjectionInstance(instance);
            bindDecorators();
            bindInterceptors();
            injectEjbAndCommonFields(instance);
            injectBoundFields(instance);
            callInitializers(instance);
            callPostConstruct(instance);
         }
         finally
         {
            injectionPointProvider.clearCurrentInjectionInstance(instance);
            injectionPointProvider.popBean();
         }
         return instance;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   /**
    * Destroys an instance of the bean
    * 
    * @param instance The instance
    */
   @Override
   public void destroy(T instance)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         callPreDestroy(instance);
         dependentInstancesStore.destroyDependentInstances(instance);
      }
      catch (Exception e)
      {
         log.error("Error destroying " + toString(), e);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   /**
    * Calls the pre-destroy method, if any
    * 
    * @param instance The instance to invoke the method on
    */
   protected void callPreDestroy(T instance)
   {
      AnnotatedMethod<?> preDestroy = getPreDestroy();
      if (preDestroy != null)
      {
         try
         {
            // note: RI supports injection into @PreDestroy
            preDestroy.invoke(instance, manager);
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
      AnnotatedMethod<?> postConstruct = getPostConstruct();
      if (postConstruct != null)
      {
         try
         {
            // note: RI supports injection into @PostConstruct
            postConstruct.invoke(instance, manager);
         }
         catch (Exception e)
         {
            throw new RuntimeException("Unable to invoke " + postConstruct + " on " + instance, e);
         }
      }
   }

   /**
    * Calls any initializers
    * 
    * @param instance The instance to invoke the initializers on
    */
   protected void callInitializers(T instance)
   {
      for (AnnotatedMethod<?> initializer : getInitializerMethods())
      {
         initializer.invoke(instance, manager);
      }
   }

   /**
    * Injects EJBs and common fields
    */
   protected void injectEjbAndCommonFields(T beanInstance)
   {
      for (AnnotatedField<?> field : annotatedItem.getAnnotatedFields(manager.getEjbResolver().getEJBAnnotation()))
      {
         InjectionPoint injectionPoint = InjectionPointImpl.of(field, this);
         Object ejbInstance = manager.getEjbResolver().resolveEjb(injectionPoint, manager.getNaming());
         field.inject(beanInstance, ejbInstance);
      }

      for (AnnotatedMethod<?> method : annotatedItem.getAnnotatedMethods(manager.getEjbResolver().getEJBAnnotation()))
      {
         InjectionPoint injectionPoint = InjectionPointImpl.of(method, this);
         Object ejbInstance = manager.getEjbResolver().resolveEjb(injectionPoint, manager.getNaming());
         method.invoke(beanInstance, ejbInstance);
      }

      for (AnnotatedField<?> field : annotatedItem.getAnnotatedFields(manager.getEjbResolver().getPersistenceContextAnnotation()))
      {
         if (field.getAnnotation(PersistenceContext.class).type().equals(PersistenceContextType.EXTENDED))
         {
            throw new ExecutionException("Cannot inject an extended persistence context into " + field);
         }
         InjectionPoint injectionPoint = InjectionPointImpl.of(field, this);
         Object puInstance = manager.getEjbResolver().resolvePersistenceContext(injectionPoint, manager.getNaming());
         field.inject(beanInstance, puInstance);
      }

      for (AnnotatedMethod<?> method : annotatedItem.getAnnotatedMethods(manager.getEjbResolver().getPersistenceContextAnnotation()))
      {
         InjectionPoint injectionPoint = InjectionPointImpl.of(method, this);
         Object puInstance = manager.getEjbResolver().resolvePersistenceContext(injectionPoint, manager.getNaming());
         method.invoke(beanInstance, puInstance);
      }

      for (AnnotatedField<?> field : annotatedItem.getAnnotatedFields(manager.getEjbResolver().getResourceAnnotation()))
      {
         InjectionPoint injectionPoint = InjectionPointImpl.of(field, this);
         Object resourceInstance = manager.getEjbResolver().resolveResource(injectionPoint, manager.getNaming());
         field.inject(beanInstance, resourceInstance);
      }

   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      initConstructor();
      checkType();
      initInjectionPoints();
      initPostConstruct();
      initPreDestroy();
      // TODO Interceptors
   }

   /**
    * Initializes the injection points
    */
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (AnnotatedParameter<?> parameter : constructor.getParameters())
      {
         annotatedInjectionPoints.add(parameter);
      }
      for (AnnotatedMethod<?> initializer : getInitializerMethods())
      {
         for (AnnotatedParameter<?> parameter : initializer.getParameters())
         {
            annotatedInjectionPoints.add(parameter);
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
         throw new DefinitionException("Simple Web Bean " + type + " cannot be a non-static inner class");
      }
      if (getAnnotatedItem().isParameterizedType())
      {
         throw new DefinitionException("Simple Web Bean " + type + " cannot be a parameterized type");
      }
      boolean passivating = MetaDataCache.instance().getScopeModel(scopeType).isPassivating();
      if (passivating && !Reflections.isSerializable(type))
      {
         throw new DefinitionException("Simple Web Beans declaring a passivating scope must have a serializable implementation class");
      }
   }
   
   @Override
   protected void checkBeanImplementation()
   {
      super.checkBeanImplementation();
      if (!scopeType.equals(Dependent.class))
      {
         for (AnnotatedField<?> field : getAnnotatedItem().getFields())
         {
            if (field.isPublic() && !field.isStatic())
            {
               throw new DefinitionException("Normal scoped Web Bean implementation class has a public field " + getAnnotatedItem());
            }
         }
      }
   }

   /**
    * Initializes the constructor
    */
   protected void initConstructor()
   {
      Set<AnnotatedConstructor<T>> initializerAnnotatedConstructors = getAnnotatedItem().getAnnotatedConstructors(Initializer.class);
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
         this.constructor = initializerAnnotatedConstructors.iterator().next();
         log.trace("Exactly one constructor (" + constructor + ") annotated with @Initializer defined, using it as the bean constructor for " + getType());
         return;
      }

      if (getAnnotatedItem().getConstructor(NO_ARGUMENTS) != null)
      {

         this.constructor = getAnnotatedItem().getConstructor(NO_ARGUMENTS);
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
      Set<AnnotatedMethod<?>> postConstructMethods = getAnnotatedItem().getAnnotatedMethods(PostConstruct.class);
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
      Set<AnnotatedMethod<?>> preDestroyMethods = getAnnotatedItem().getAnnotatedMethods(PreDestroy.class);
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
    * Initializes the bean type
    */
   protected void initType()
   {
      log.trace("Bean type specified in Java");
      this.type = getAnnotatedItem().getType();
   }

   /**
    * Returns the constructor
    * 
    * @return The constructor
    */
   public AnnotatedConstructor<T> getConstructor()
   {
      return constructor;
   }

   /**
    * Returns the specializes type of the bean
    * 
    * @return The specialized type
    */
   @SuppressWarnings("unchecked")
   protected AbstractBean<? extends T, Class<T>> getSpecializedType()
   {
      // TODO lots of validation!
      Class<?> superclass = getAnnotatedItem().getType().getSuperclass();
      if (superclass != null)
      {
         // TODO look up this bean and do this via init
         return (SimpleBean) SimpleBean.of(superclass, manager);
      }
      else
      {
         throw new RuntimeException();
      }
   }

   /**
    * Returns the post-construct method
    * 
    * @return The post-construct method
    */
   public AnnotatedMethod<?> getPostConstruct()
   {
      return postConstruct;
   }

   /**
    * Returns the pre-destroy method
    * 
    * @return The pre-destroy method
    */
   public AnnotatedMethod<?> getPreDestroy()
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
      buffer.append("Annotated " + Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append("unnamed simple bean");
      }
      else
      {
         buffer.append("simple bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "]\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindings() + "\n");
      return buffer.toString();
   }

   /**
    * Indicates if the bean is serializable
    * 
    * @return true If serializable, false otherwise
    */
   @Override
   public boolean isSerializable()
   {
      boolean dependent = Dependent.class.equals(getScopeType());
      if (dependent)
      {
         return Reflections.isSerializable(getType());
      }
      else
      {
         return injectionPointsAreSerializable();
      }
   }

}
