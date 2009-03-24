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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.context.CreationalContext;
import javax.context.Dependent;
import javax.context.ScopeType;
import javax.inject.DefinitionException;
import javax.inject.DeploymentType;
import javax.inject.IllegalProductException;
import javax.inject.Initializer;
import javax.inject.Produces;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.context.CreationalContextImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.DependentStorageRequest;
import org.jboss.webbeans.introspector.AnnotatedMember;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.Reflections;

/**
 * The implicit producer bean
 * 
 * @author Gavin King
 * 
 * @param <T>
 * @param <S>
 */
public abstract class AbstractProducerBean<T, S extends Member> extends AbstractBean<T, S>
{
   // The declaring bean
   protected AbstractClassBean<?> declaringBean;

   private static final LogProvider log = Logging.getLogProvider(AbstractProducerBean.class);

   /**
    * Constructor
    * 
    * @param declaringBean The declaring bean
    * @param manager The Web Beans manager
    */
   public AbstractProducerBean(AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      super(manager);
      this.declaringBean = declaringBean;
   }
   
   protected abstract AnnotatedMember<T, S> getAnnotatedItem();

   /**
    * Gets the deployment types
    * 
    * @return The deployment types of the declaring bean
    */
   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return deploymentType = declaringBean.getDeploymentType();
   }

   /**
    * Initializes the API types
    */
   @Override
   protected void initTypes()
   {
      if (getType().isArray() || getType().isPrimitive())
      {
         Set<Type> types = new HashSet<Type>();
         types = new HashSet<Type>();
         types.add(getType());
         types.add(Object.class);
         super.types = types;
      }
      else if (getType().isInterface())
      {
         Set<Type> types = new HashSet<Type>();
         types.add(Object.class);
         types.addAll(getAnnotatedItem().getFlattenedTypeHierarchy());
         super.types = types;
      }
      else
      {
         super.initTypes();
      }
   }

   /**
    * Initializes the type
    */
   protected void initType()
   {
      try
      {
         this.type = getAnnotatedItem().getRawType();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeException(" Cannot cast producer type " + getAnnotatedItem().getRawType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
   }

   /**
    * Gets the declared bean type
    * 
    * @return The bean type
    */
   protected Type getDeclaredBeanType()
   {
      Type type = getClass();
      if (type instanceof ParameterizedType)
      {
         ParameterizedType parameterizedType = (ParameterizedType) type;
         if (parameterizedType.getActualTypeArguments().length == 1)
         {
            return parameterizedType.getActualTypeArguments()[0];
         }
      }
      return null;
   }

   /**
    * Returns the declaring bean
    * 
    * @return The bean representation
    */
   public AbstractClassBean<?> getDeclaringBean()
   {
      return declaringBean;
   }

   /**
    * Validates the producer method
    */
   protected void checkProducerReturnType()
   {
      for (Type type : getAnnotatedItem().getActualTypeArguments())
      {
         if (!(type instanceof Class))
         {
            throw new DefinitionException("Producer type cannot be parameterized with type parameter or wildcard:\n" + this.getAnnotatedItem());
         }
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   public void initialize(BeanDeployerEnvironment environment)
   {
      declaringBean.initialize(environment);
      super.initialize(environment);
      checkProducerReturnType();
   }

   /**
    * Validates the return value
    * 
    * @param instance The instance to validate
    */
   protected void checkReturnValue(T instance)
   {
      if (instance == null && !isDependent())
      {
         throw new IllegalProductException("Cannot return null from a non-dependent producer method");
      }
      boolean passivating = MetaDataCache.instance().getScopeModel(getScopeType()).isPassivating();
      if (passivating && !Reflections.isSerializable(instance.getClass()))
      {
         throw new IllegalProductException("Producers cannot declare passivating scope and return a non-serializable class");
      }
      InjectionPoint injectionPoint = manager.getInjectionPoint();
      if (injectionPoint == null)
      {
         return;
      }
      if (isDependent() && Beans.isPassivatingBean(injectionPoint.getBean()))
      {
         if (injectionPoint.getMember() instanceof Field)
         {
            if (!Reflections.isTransient(injectionPoint.getMember()) && !Reflections.isSerializable(instance.getClass()))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into non-transient fields of passivating beans\n\nProducer: " + this.toString() + "\nInjection Point: " + injectionPoint.toString());
            }
         }
         else if (injectionPoint.getMember() instanceof Method)
         {
            Method method = (Method) injectionPoint.getMember();
            if (method.isAnnotationPresent(Initializer.class))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of intializers of beans declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
            }
            if (method.isAnnotationPresent(Produces.class))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of producer methods declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
            }
         }
         else if (injectionPoint.getMember() instanceof Constructor)
         {
            throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of constructors of beans declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
         }
      }
   }

   @Override
   protected void initScopeType()
   {
      Set<Annotation> scopeAnnotations = getAnnotatedItem().getMetaAnnotations(ScopeType.class);
      if (scopeAnnotations.size() > 1)
      {
         throw new DefinitionException("At most one scope may be specified");
      }
      if (scopeAnnotations.size() == 1)
      {
         this.scopeType = scopeAnnotations.iterator().next().annotationType();
         log.trace("Scope " + scopeType + " specified by annotation");
         return;
      }

      initScopeTypeFromStereotype();

      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace("Using default @Dependent scope");
      }
   }

   @Override
   protected void initDeploymentType()
   {
      Set<Annotation> deploymentTypes = getAnnotatedItem().getMetaAnnotations(DeploymentType.class);
      if (deploymentTypes.size() > 1)
      {
         throw new DefinitionException("At most one deployment type may be specified (" + deploymentTypes + " are specified) on " + getAnnotatedItem().toString());
      }
      else if (deploymentTypes.size() == 1)
      {
         this.deploymentType = deploymentTypes.iterator().next().annotationType();
         log.trace("Deployment type " + deploymentType + " specified by annotation");
         return;
      }

      initDeploymentTypeFromStereotype();

      if (this.deploymentType == null)
      {
         this.deploymentType = getDefaultDeploymentType();
         log.trace("Using default " + this.deploymentType + " deployment type");
         return;
      }
   }

   @Override
   protected void initSerializable()
   {
      _serializable = true;
   }

   /**
    * Gets the receiver of the product
    * 
    * @return The receiver
    */
   protected Object getReceiver(CreationalContext<?> creationalContext)
   {
      // This is a bit dangerous, as it means that producer methods can end of
      // executing on partially constructed instances. Also, it's not required
      // by the spec...
      if (getAnnotatedItem().isStatic())
      {
         return null;
      }
      else
      {
         if (creationalContext instanceof CreationalContextImpl)
         {
            CreationalContextImpl<?> creationalContextImpl = (CreationalContextImpl<?>) creationalContext;
            if (creationalContextImpl.containsIncompleteInstance(getDeclaringBean()))
            {
               log.warn("Executing producer field or method " + getAnnotatedItem() + " on incomplete declaring bean " + getDeclaringBean() + " due to circular injection");
               return creationalContextImpl.getIncompleteInstance(getDeclaringBean());
            }
         }
         return manager.getInstance(getDeclaringBean());
      }
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   public T create(CreationalContext<T> creationalContext)
   {
      DependentStorageRequest dependentStorageRequest = DependentStorageRequest.of(dependentInstancesStore, new Object());
      try
      {
         if (getDeclaringBean().isDependent())
         {
            DependentContext.INSTANCE.startCollectingDependents(dependentStorageRequest);
         }
         DependentContext.INSTANCE.setActive(true);
         T instance = produceInstance(creationalContext);
         checkReturnValue(instance);
         return instance;
      }
      finally
      {
         if (getDeclaringBean().isDependent())
         {
            DependentContext.INSTANCE.stopCollectingDependents(dependentStorageRequest);
            dependentInstancesStore.destroyDependentInstances(dependentStorageRequest.getKey());
         }
         DependentContext.INSTANCE.setActive(false);
      }
   }

   public void destroy(T instance)
   {
      /*
       * try { DependentContext.INSTANCE.setActive(true); } finally {
       * DependentContext.INSTANCE.setActive(false); }
       */
   }

   protected abstract T produceInstance(CreationalContext<T> creationalContext);

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
         buffer.append("unnamed producer bean");
      }
      else
      {
         buffer.append("simple producer bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "] API types " + getTypes() + ", binding types " + getBindings());
      return buffer.toString();
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof AbstractProducerBean)
      {
         AbstractProducerBean<?, ?> that = (AbstractProducerBean<?, ?>) other;
         return super.equals(other) && this.getAnnotatedItem().getDeclaringClass().equals(that.getAnnotatedItem().getDeclaringClass());
      }
      else
      {
         return false;
      }
   }

}