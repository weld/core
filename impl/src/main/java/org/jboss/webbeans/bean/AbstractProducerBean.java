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
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.IllegalProductException;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Inject;
import javax.inject.Scope;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bootstrap.BeanDeployerEnvironment;
import org.jboss.webbeans.introspector.WBMember;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
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
public abstract class AbstractProducerBean<T, S extends Member> extends AbstractReceiverBean<T, S> implements Producer<T>
{
   private static final LogProvider log = Logging.getLogProvider(AbstractProducerBean.class);

   /**
    * Constructor
    * @param declaringBean The declaring bean
    * @param manager The Web Beans manager
    */
   public AbstractProducerBean(String idSuffix, AbstractClassBean<?> declaringBean, BeanManagerImpl manager)
   {
      super(idSuffix, declaringBean, manager);
   }

   @Override
   protected abstract WBMember<T, ?, S> getAnnotatedItem();

   @Override
   // Overriden to provide the class of the bean that declares the producer method/field
   public Class<?> getBeanClass()
   {
      return getDeclaringBean().getBeanClass();
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
         types.addAll(getAnnotatedItem().getTypeClosure());
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
         this.type = getAnnotatedItem().getJavaClass();
      }
      catch (ClassCastException e)
      {
         throw new RuntimeException(" Cannot cast producer type " + getAnnotatedItem().getJavaClass() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
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
    * Validates the producer method
    */
   protected void checkProducerReturnType()
   {
      if (getAnnotatedItem().getBaseType() instanceof TypeVariable<?>)
      {
         throw new DefinitionException("Return type must be concrete " + getAnnotatedItem().getBaseType());
      }
      if (getAnnotatedItem().getBaseType() instanceof WildcardType)
      {
         throw new DefinitionException("Return type must be concrete " + getAnnotatedItem().getBaseType());
      }
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
      getDeclaringBean().initialize(environment);
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
      else if (instance != null)
      {
         boolean passivating = manager.getServices().get(MetaAnnotationStore.class).getScopeModel(getScope()).isPassivating();
         if (passivating && !Reflections.isSerializable(instance.getClass()))
         {
            throw new IllegalProductException("Producers cannot declare passivating scope and return a non-serializable class");
         }
         InjectionPoint injectionPoint = manager.getCurrentInjectionPoint();
         if (injectionPoint == null)
         {
            return;
         }
         if (!Reflections.isSerializable(instance.getClass()) && Beans.isPassivationCapableBean(injectionPoint.getBean()))
         {
            if (injectionPoint.getMember() instanceof Field)
            {
               if (!Reflections.isTransient(injectionPoint.getMember()) && instance != null && !Reflections.isSerializable(instance.getClass()))
               {
                  throw new IllegalProductException("Producers cannot produce non-serializable instances for injection into non-transient fields of passivating beans\n\nProducer: " + this.toString() + "\nInjection Point: " + injectionPoint.toString());
               }
            }
            else if (injectionPoint.getMember() instanceof Method)
            {
               Method method = (Method) injectionPoint.getMember();
               if (method.isAnnotationPresent(Inject.class))
               {
                  throw new IllegalProductException("Producers cannot produce non-serializable instances for injection into parameters of intializers of beans declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
               }
               if (method.isAnnotationPresent(Produces.class))
               {
                  throw new IllegalProductException("Producers cannot produce non-serializable instances for injection into parameters of producer methods declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
               }
            }
            else if (injectionPoint.getMember() instanceof Constructor)
            {
               throw new IllegalProductException("Producers cannot produce non-serializable instances for injection into parameters of constructors of beans declaring passivating scope. Bean " + toString() + " being injected into " + injectionPoint.toString());
            }
         }
      }
   }

   @Override
   protected void initScopeType()
   {
      Set<Annotation> scopeAnnotations = new HashSet<Annotation>();
      scopeAnnotations.addAll(getAnnotatedItem().getMetaAnnotations(Scope.class));
      scopeAnnotations.addAll(getAnnotatedItem().getMetaAnnotations(NormalScope.class));
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
   protected void initSerializable()
   {
      // No-op
   }

   @Override
   public boolean isSerializable()
   {
      return true;
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   public T create(final CreationalContext<T> creationalContext)
   {
      try
      {
         T instance = produce(creationalContext);
         checkReturnValue(instance);
         return instance;
      }
      finally
      {
         if (getDeclaringBean().isDependent())
         {
            creationalContext.release();
         }
      }
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String getDescription()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Annotated " + Names.scopeTypeToString(getScope()));
      if (getName() == null)
      {
         buffer.append("unnamed producer bean");
      }
      else
      {
         buffer.append("simple producer bean '" + getName() + "'");
      }
      buffer.append(" [" + getBeanClass().getName() + "] for class type [" + getType().getName() + "] API types " + getTypes() + ", binding types " + getQualifiers());
      return buffer.toString();
   }

}