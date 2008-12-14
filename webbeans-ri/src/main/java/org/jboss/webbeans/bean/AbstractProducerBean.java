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
import java.lang.reflect.Type;
import java.util.HashSet;

import javax.webbeans.DefinitionException;
import javax.webbeans.Dependent;
import javax.webbeans.IllegalProductException;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.util.Names;

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
   protected void initApiTypes()
   {
      if (getType().isArray() || getType().isPrimitive())
      {
         apiTypes = new HashSet<Class<?>>();
         apiTypes.add(getType());
         apiTypes.add(Object.class);
      }
      else if (getType().isInterface())
      {
         super.initApiTypes();
         apiTypes.add(Object.class);
      }
      else
      {
         super.initApiTypes();
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
      if (instance == null && !getScopeType().equals(Dependent.class))
      {
         throw new IllegalProductException("Cannot return null from a non-dependent producer method");
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
      buffer.append("   API types " + getTypes() + ", binding types " + getBindingTypes() + "\n");
      return buffer.toString();
   }

}