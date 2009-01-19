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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.IllegalProductException;
import javax.webbeans.Initializer;
import javax.webbeans.Produces;
import javax.webbeans.ScopeType;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.injection.InjectionPointImpl;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
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
public abstract class AbstractProducerBean<T, S> extends AbstractBean<T, S>
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
         types = new HashSet<Type>();
         types.add(getType());
         types.add(Object.class);
      }
      else if (getType().isInterface())
      {
         super.initTypes();
         types.add(Object.class);
      }
      else
      {
         super.initTypes();
      }
   }

   /**
    * Initializes the type
    */
   @Override
   protected void initType()
   {
      try
      {
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e)
      {
         throw new RuntimeException(" Cannot cast producer type " + getAnnotatedItem().getType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
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
            throw new DefinitionException("Producer type cannot be parameterized with type parameter or wildcard");
         }
      }
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      checkProducerReturnType();
   }

   /**
    * Validates the return value
    * 
    * @param instance The instance to validate
    */
   protected void checkReturnValue(T instance)
   {
      boolean dependent = Dependent.class.equals(getScopeType());
      if (instance == null && !dependent)
      {
         throw new IllegalProductException("Cannot return null from a non-dependent producer method");
      }
      boolean passivating = MetaDataCache.instance().getScopeModel(getScopeType()).isPassivating();
      if (passivating && !Reflections.isSerializable(instance.getClass()))
      {
         throw new IllegalProductException("Producers cannot declare passivating scope and return a non-serializable class");
      }
      InjectionPointImpl injectionPoint = (InjectionPointImpl) manager.getInjectionPointProvider().getCurrentInjectionPoint();
      if (injectionPoint == null)
      {
         return;
      }
      if (dependent && Reflections.isPassivatingBean(injectionPoint.getBean()))
      {
         if (injectionPoint.isField())
         {
            if (!Reflections.isTransient(injectionPoint.getMember()))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into non-transient fields of passivating beans");
            }
         }
         else if (injectionPoint.isMethod())
         {
            Method method = (Method) injectionPoint.getMember();
            if (method.isAnnotationPresent(Initializer.class))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of intializers of beans declaring passivating scope");
            }
            if (method.isAnnotationPresent(Produces.class))
            {
               throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of producer methods declaring passivating scope");
            }
         }
         else if (injectionPoint.isConstructor())
         {
            throw new IllegalProductException("Dependent scoped producers cannot produce non-serializable instances for injection into parameters of constructors of beans declaring passivating scope");
         }
         else
         {
            // TODO: possible case?
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
      
      initScopeFromStereotype();
      
      if (this.scopeType == null)
      {
         this.scopeType = Dependent.class;
         log.trace("Using default @Dependent scope");
      }
   }

   /**
    * Gets the receiver of the product
    * 
    * @return The receiver
    */
   protected Object getReceiver()
   {
      return getAnnotatedItem().isStatic() ? null : manager.getInstance(getDeclaringBean());
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   @Override
   public T create()
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         T instance = produceInstance();
         checkReturnValue(instance);
         return instance;
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   @Override
   public void destroy(T instance)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         // TODO Implement any cleanup needed
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }

   protected abstract T produceInstance();

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
      buffer.append(" [" + getType().getName() + "]\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindings() + "\n");
      return buffer.toString();
   }

}