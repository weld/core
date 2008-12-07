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
import javax.webbeans.Destructor;
import javax.webbeans.Disposes;
import javax.webbeans.IllegalProductException;
import javax.webbeans.Observes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.introspector.jlr.AnnotatedMethodImpl;

/**
 * Represents a producer method bean
 * 
 * @author Pete Muir
 *
 * @param <T>
 */
public class ProducerMethodBean<T> extends AbstractBean<T, Method>
{
   
   private AnnotatedMethod<T> method;
   private AbstractClassBean<?> declaringBean;

   /**
    * Constructor
    * 
    * @param method The producer method
    * @param declaringBean The declaring bean instance
    */
   public ProducerMethodBean(Method method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      this(new AnnotatedMethodImpl<T>(method, declaringBean.getAnnotatedItem()), declaringBean, manager);
   }
   
   /**
    * Constructor
    * 
    * @param method The producer method abstraction
    * @param declaringBean The declaring bean
    */
   public ProducerMethodBean(AnnotatedMethod<T> method, AbstractClassBean<?> declaringBean, ManagerImpl manager)
   {
      super(manager);
      this.method = method;
      this.declaringBean = declaringBean;
      init();
   }

   /**
    * Creates an instance of the bean
    * 
    * @returns The instance
    */
   @Override
   public T create()
   {
      T instance = method.invoke(manager, manager.getInstance(getDeclaringBean()));
      if (instance == null && !getScopeType().equals(Dependent.class))
      {
         throw new IllegalProductException("Cannot return null from a non-dependent method");
      }
      return instance;
   }

   /**
    * Initializes the bean and its metadata
    */
   @Override
   protected void init()
   {
      super.init();
      checkProducerMethod();
      initRemoveMethod();
      initInjectionPoints();
   }
   
   /**
    * Initializes the injection points
    */   
   @Override
   protected void initInjectionPoints()
   {
      super.initInjectionPoints();
      for (AnnotatedParameter<Object> parameter : method.getParameters())
      {
         injectionPoints.add(parameter);
      }
      if (removeMethod != null)
      {
         for (AnnotatedParameter<?> injectable : removeMethod.getParameters())
         {
            injectionPoints.add(injectable);
         }
      }
   }
   
   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return deploymentType = declaringBean.getDeploymentType();
   }
   
   /**
    * Validates the producer method
    */
   protected void checkProducerMethod()
   {
      if (getAnnotatedItem().isStatic())
      {
         throw new DefinitionException("Producer method cannot be static " + method);
      }
      else if (getAnnotatedItem().isAnnotationPresent(Destructor.class))
      {
         throw new DefinitionException("Producer method cannot be annotated @Destructor");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Observes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Observes");
      }
      else if (getAnnotatedItem().getAnnotatedParameters(Disposes.class).size() > 0)
      {
         throw new DefinitionException("Producer method cannot have parameter annotated @Disposes");
      }
      else if (getAnnotatedItem().getActualTypeArguments().length > 0)
      {
         for (Type type : getAnnotatedItem().getActualTypeArguments())
         {
            if (!(type instanceof Class))
            {
               throw new DefinitionException("Producer method cannot return type parameterized with type parameter or wildcard");
            }
         }
      }
   }
   
   /**
    * Initializes the remove method
    */
   protected void initRemoveMethod()
   {
      Set<AnnotatedMethod<Object>> disposalMethods = manager.resolveDisposalMethods(getType(), getBindingTypes().toArray(new Annotation[0]));
      if (disposalMethods.size() == 1)
      {
         removeMethod = disposalMethods.iterator().next();
      }
      else if (disposalMethods.size() > 1)
      {
         // TODO List out found disposal methods
         throw new DefinitionException ("Cannot declare multiple disposal methods for this producer method");
      }
   }
   

   /**
    * Gets the annotated item representing the method
    * 
    * @return The annotated item
    */
   @Override
   protected AnnotatedMethod<T> getAnnotatedItem()
   {
      return method;
   }

   /**
    * Returns the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      return method.getPropertyName();
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
         throw new RuntimeException(" Cannot cast producer method return type " + method.getType() + " to bean type " + (getDeclaredBeanType() == null ? " unknown " : getDeclaredBeanType()), e);
      }
   }
   
   /**
    * Initializes the API types
    */
   @Override
   protected void initApiTypes()
   {
      if (getType().isArray() || getType().isPrimitive())
      {
         super.apiTypes = new HashSet<Class<?>>();
         super.apiTypes.add(getType());
         super.apiTypes.add(Object.class);
      }
      else if (getType().isInterface())
      {
         super.initApiTypes();
         super.apiTypes.add(Object.class);
      }
      else
      {
         super.initApiTypes();
      }
   }
   
   /**
    * Returns the disposal method
    * 
    * @return The method representation
    */
   public AnnotatedMethod<?> getDisposalMethod()
   {
      return removeMethod;
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

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("ProducerMethodBean:\n");
      buffer.append(super.toString() + "\n");
      buffer.append("Declaring bean: " + declaringBean.toString() + "\n");
      buffer.append("Method: " + method.toString() + "\n");
      return buffer.toString();      
   }

   
}
