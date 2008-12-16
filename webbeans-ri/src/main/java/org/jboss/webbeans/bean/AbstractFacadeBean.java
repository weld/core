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
import javax.webbeans.Dependent;
import javax.webbeans.Production;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;

/**
 * Facade bean for implicit beans
 * 
 * @author Gavin King
 * 
 * @param <T>
 * @param <S>
 * @param <P>
 */
public abstract class AbstractFacadeBean<T, S, P> extends AbstractBean<T, S>
{
   // The underlying item
   protected AnnotatedItem<T, S> annotatedItem;

   /**
    * Constructor
    * 
    * @param field The facaded field
    * @param manager The Web Beans manager
    */
   public AbstractFacadeBean(AnnotatedItem<T, S> field, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = field;
      init();
   }

   /*
    * Gets the binding type as an array
    * 
    * @return The binding types
    */
   protected Annotation[] getBindingTypesArray()
   {
      return annotatedItem.getBindingTypesAsArray();
   }

   /**
    * Gets the type paramater of the facade
    * 
    * @return The type parameter
    */
   @SuppressWarnings("unchecked")
   protected Class<P> getTypeParameter()
   {
      return (Class<P>) annotatedItem.getType().getTypeParameters()[0].getClass();
   }

   /**
    * Initializes the scope type to dependent
    */
   @Override
   protected void initScopeType()
   {
      this.scopeType = Dependent.class;
   }

   /**
    * Initializes the deployment type to Standard
    */
   @Override
   protected void initDeploymentType()
   {
      this.deploymentType = Standard.class;
   }

   /**
    * Gets the underlying item
    * 
    * @return The underlying item
    */
   @Override
   protected AnnotatedItem<T, S> getAnnotatedItem()
   {
      return annotatedItem;
   }

   /**
    * Gets the default name
    * 
    * @return The default name
    */
   @Override
   protected String getDefaultName()
   {
      return null;
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
         throw new IllegalArgumentException("Cannot create an instance of " + toString() + " with " + getAnnotatedItem());
      }
   }

   /**
    * Gets the default deployment type, Production
    */
   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType()
   {
      return Production.class;
   }

   /**
    * Returns a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return "FacadeBean " + getName() + " for " + annotatedItem;
   }

}