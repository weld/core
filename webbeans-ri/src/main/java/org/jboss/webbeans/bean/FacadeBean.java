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

import javax.webbeans.DefinitionException;
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
public abstract class FacadeBean<T, S, P> extends AbstractBean<T, S> {

   protected AnnotatedItem<T, S> annotatedItem;

   public FacadeBean(AnnotatedItem<T, S> field, ManagerImpl manager) {
      super(manager);
      this.annotatedItem = field;
      init();
   }

   /**
    * Initializes the bean
    * 
    * Calls super method and validates the annotated item
    */
   protected void init() {
      super.init();
      checkAnnotatedItem();
   }

   /**
    * Validates the annotated item
    */
   private void checkAnnotatedItem() {
      Type[] actualTypeArguments = annotatedItem.getActualTypeArguments();
      if (actualTypeArguments.length != 1)
      {
         throw new DefinitionException("Event must have type arguments");
      }
      if (!(actualTypeArguments[0] instanceof Class))
      {
         throw new DefinitionException("Event must have concrete type argument");
      }
   }

   protected Annotation[] getBindingTypesArray() {
      return annotatedItem.getBindingTypesAsArray();
   }

   @SuppressWarnings("unchecked")
   protected Class<P> getTypeParameter() {
      return (Class<P>) annotatedItem.getType().getTypeParameters()[0].getClass();
   }

   @Override
   protected void initScopeType() {
      this.scopeType = Dependent.class;
   }

   @Override
   protected void initDeploymentType() {
      this.deploymentType = Standard.class;
   }

   @Override
   protected AnnotatedItem<T, S> getAnnotatedItem() {
      return annotatedItem;
   }

   @Override
   protected String getDefaultName() {
      return null;
   }

   @Override
   protected void initType() {
      try
      {
         if (getAnnotatedItem() != null)
         {
            this.type = getAnnotatedItem().getType();
         }
      }
      catch (ClassCastException e)
      {
         // TODO: Expand error
         throw new IllegalArgumentException("Type mismatch");
      }
   }

   @Override
   protected Class<? extends Annotation> getDefaultDeploymentType() {
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
      return "FacadeBean " + getName();
   }   

}