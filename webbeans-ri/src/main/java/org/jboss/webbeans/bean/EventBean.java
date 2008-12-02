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
import org.jboss.webbeans.introspector.jlr.AnnotatedFieldImpl;
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

   private String location;
   private AnnotatedField<EventImpl<T>> annotatedItem;

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
   protected void init() {
      super.init();
      checkAnnotatedItem();
   }
   
   /**
    * Validates the annotated item
    */
   private void checkAnnotatedItem() {
      // TODO: checks
   }
   
   /**
    * Caches the constructor for this type of bean to avoid future reflections
    * during use.
    */
   @SuppressWarnings("unused")
   private void initConstructor()
   {
      try
      {
         // constructor = new SimpleConstructor<T>((Constructor<T>)
         // EventImpl.class.getConstructor((Class[])null));
      }
      catch (Exception e)
      {
         log.warn("Unable to get constructor for build-in Event implementation", e);
      }
   }

   /*
    * public BeanConstructor<T> getConstructor() { return constructor; }
    */

   public String getLocation()
   {
      if (location == null)
      {
         location = "type: Event Bean;";
      }
      return location;
   }

   @Override
   public String toString()
   {
      return "EventBean[" + getType().getName() + "]";
   }

   @Override
   protected void initType()
   {
      log.trace("Bean type specified in Java");
      this.type = annotatedItem.getType();
   }

   @Override
   protected AnnotatedItem<EventImpl<T>, Field> getAnnotatedItem()
   {
      return annotatedItem;
   }

   @Override
   protected String getDefaultName()
   {
      // No name per 7.4
      return null;
   }

   @Override
   protected void initDeploymentType()
   {
      // This is always @Standard per 7.4
      this.deploymentType = Standard.class;
   }

   @Override
   protected void checkDeploymentType()
   {
      // No - op
   }

   @Override
   protected void initName()
   {
      // No name per 7.4
      this.name = null;
   }

   @Override
   protected void initScopeType()
   {
      // This is always @Dependent per 7.4
      this.scopeType = Dependent.class;
   }

   @Override
   public EventImpl<T> create()
   {
      return new EventImpl<T>();
   }

}
