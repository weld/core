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


import java.lang.reflect.Type;

import javax.webbeans.DefinitionException;
import javax.webbeans.Event;

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
 * @param <T>
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
      if (!this.getAnnotatedItem().getType().equals(Event.class))
      {
         throw new DefinitionException("Observable field/parameter is not of type Event<T>: " + this.getAnnotatedItem());
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
   @SuppressWarnings("unchecked")
   @Override
   public Event<T> create()
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         Class eventType = null;
         if (this.getAnnotatedItem() instanceof AnnotatedParameter)
         {
            eventType = Object.class;
         } else
         {
            eventType = (Class<T>) getAnnotatedItem().getActualTypeArguments()[0];         
         }
         return new EventImpl(eventType, manager, getBindingTypesArray());
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
   }
   
   @Override
   public void destroy(Event<T> instance)
   {
      try
      {
         DependentContext.INSTANCE.setActive(true);
         // TODO Implement any EventBean destruction needed
      }
      finally
      {
         DependentContext.INSTANCE.setActive(false);
      }
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
      buffer.append("Annotated " + Names.scopeTypeToString(getScopeType()));
      if (getName() == null)
      {
         buffer.append(" unnamed event bean");
      }
      else
      {
         buffer.append(" enterprise bean '" + getName() + "'");
      }
      buffer.append(" [" + getType().getName() + "]\n");
      buffer.append("   API types " + getTypes() + ", binding types " + getBindings() + "\n");
      return buffer.toString();
   } 

}
