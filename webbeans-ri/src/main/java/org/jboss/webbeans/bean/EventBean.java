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


import javax.webbeans.Event;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.event.EventImpl;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.util.Names;

/**
 * An event bean representation
 * 
 * @author David Allen
 * 
 * @param <T>
 * @param <S>
 */
public class EventBean<T, S> extends FacadeBean<Event<T>, S, T>
{

   /**
    * Constructor
    * 
    * @param field The underlying field abstraction
    * @param manager The Web Beans manager
    */
   public EventBean(AnnotatedItem<Event<T>, S> field, ManagerImpl manager)
   {
      super(field, manager);
   }

   /**
    * Creates an instance
    * 
    * @return an event instance
    */
   @Override
   public Event<T> create()
   {
      return new EventImpl<T>(getTypeParameter(), manager, getBindingTypesArray());
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
      buffer.append("   API types " + getTypes() + ", binding types " + getBindingTypes() + "\n");
      return buffer.toString();
   } 

}
