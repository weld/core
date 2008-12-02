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

import java.lang.reflect.Field;

import javax.webbeans.Dependent;
import javax.webbeans.Standard;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * An event bean representation
 * 
 * @author David Allen
 * 
 * @param <T>
 */
public class EventBean<T> extends AbstractBean<EventImpl<T>, Field>
{

   private static LogProvider log = Logging.getLogProvider(EventBean.class);

   // The debug location
   private String location;
   // The underlying annotated item
   private AnnotatedField<EventImpl<T>> annotatedItem;

   /**
    * Constructor
    * 
    * @param field The underlying field abstraction
    * @param declaringBean
    * @param manager The Web Beans manager
    */
   @SuppressWarnings("unchecked")
   public EventBean(AnnotatedField<T> field, ManagerImpl manager)
   {
      super(manager);
      this.annotatedItem = (AnnotatedField<EventImpl<T>>) field;
      init();
   }

   /**
    * Initializes the bean
    * 
    * Calls super method and validates the annotated item
    */
   protected void init()
   {
      super.init();
      checkAnnotatedItem();
   }

   /**
    * Validates the annotated item
    */
   private void checkAnnotatedItem()
   {
      // TODO: checks
   }

   /**
    * @see org.jboss.webbeans.bean.AbstractBean#initScopeType()
    */
   @Override
   protected void initScopeType()
   {
      this.scopeType = Dependent.class;
   }

   /**
    * @see org.jboss.webbeans.bean.AbstractBean#initDeploymentType()
    */
   @Override
   protected void initDeploymentType()
   {
      this.deploymentType = Standard.class;
   }

   /**
    * @see org.jboss.webbeans.bean.AbstractBean#getAnnotatedItem()
    */
   @Override
   protected AnnotatedItem<EventImpl<T>, Field> getAnnotatedItem()
   {
      return annotatedItem;
   }

   /**
    * @see org.jboss.webbeans.bean.AbstractBean#getDefaultName()
    */
   @Override
   protected String getDefaultName()
   {
      return null;
   }

   /**
    * @see org.jboss.webbeans.bean.AbstractBean#initType()
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
         // TODO: Expand error
         throw new IllegalArgumentException("Type mismatch");
      }
   }

   /**
    * Gets the debug location
    * 
    * @return A string describing the location
    */
   private String getLocation()
   {
      if (location == null)
      {
         location = "type: Event Bean;";
      }
      return location;
   }

   /**
    * @see javax.webbeans.manager.Bean#create()
    */
   @Override
   public EventImpl<T> create()
   {
      return new EventImpl<T>(getManager(), annotatedItem.getBindingTypesAsArray());
   }

}
