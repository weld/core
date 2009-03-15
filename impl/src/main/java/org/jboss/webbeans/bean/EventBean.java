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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import javax.context.CreationalContext;
import javax.event.Event;
import javax.inject.DefinitionException;
import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.util.Names;

/**
 * An event bean representation
 * 
 * @author David Allen
 * 
 * @param <T> The actual type of the event object
 * @param <S>
 */
public class EventBean<T, S> extends AbstractFacadeBean<Event<T>, S, T>
{

   /**
    * Creates an event Web Bean
    * 
    * @param field The event injection point abstraction
    * @param manager the current manager
    * @param declaringBean The declaring bean abstraction
    * @return An event Web Bean
    */
   public static <T, S> EventBean<T, S> of(AnnotatedItem<Event<T>, S> field, ManagerImpl manager)
   {
      return new EventBean<T, S>(field, manager);
   }
   
   /**
    * Constructor
    * 
    * @param field The underlying field abstraction
    * @param manager The Web Beans manager
    */
   protected EventBean(AnnotatedItem<Event<T>, S> field, ManagerImpl manager)
   {
      super(field, manager);
   }

   /**
    * Initializes the bean
    * 
    * Calls super method and validates the annotated item
    */
   protected void init()
   {
      validateInjectionPoint();
      super.init();
      checkAnnotatedItem();
   }

   /**
    * Performs early validation on the annotated item to make
    * certain the field or parameter is of the proper type.
    */
   private void validateInjectionPoint()
   {
      if (!this.getAnnotatedItem().getRawType().equals(Event.class))
      {
         throw new DefinitionException("Event field/parameter is not of type Event<T>: " + this.getAnnotatedItem());
      }
      
   }

   /**
    * Validates the annotated item
    */
   private void checkAnnotatedItem()
   {
      // Only check the type arguments if this is for a field.  Parameters
      // do not have access to the type arguments in Java 6.
      if (!(this.annotatedItem instanceof AnnotatedParameter))
      {
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
   }

   /**
    * Creates an instance
    * 
    * @return an event instance
    */
   public Event<T> create(CreationalContext<Event<T>> creationalContext)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         //TODO Fix to use IP's manager rather than this bean's
         InjectionPoint injectionPoint = this.getManager().getInjectionPoint();
         Class<?> clazz = Object.class;
         Type genericType = injectionPoint.getType().getClass().getGenericSuperclass();
         if (genericType instanceof ParameterizedType )
         {
            ParameterizedType type = (ParameterizedType) genericType;
            clazz = Class.class.cast(type.getActualTypeArguments()[0]);  
         }
                
         // TODO should be able to move this up into annotated item?!     
         @SuppressWarnings("unchecked")
         Class<T> eventType = (Class<T>) clazz;
         
         return new EventImpl<T>(eventType, manager, injectionPoint.getBindings().toArray(new Annotation[0]));
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
   
   public void destroy(Event<T> instance)
   {
      /*try
      {
         DependentContext.INSTANCE.setActive(true);
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }*/
   }
   
   /**
    * Adds additional bindings to this bean.  All bindings must be
    * finalized before the bean is registered with the manager.
    * 
    * @param additionalBindings A set of additional bindings
    */
   public void addBindings(Set<Annotation> additionalBindings)
   {
      this.bindings.addAll(additionalBindings);
   }
   
   /**
    * Returns a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append(Names.scopeTypeToString(getScopeType()));
      buffer.append("Event bean ");
      buffer.append(getType().getName());
      buffer.append(" API types = ").append(Names.typesToString(getTypes())).append(", binding types = " + Names.annotationsToString(getBindings()));
      return buffer.toString();
   }

}
