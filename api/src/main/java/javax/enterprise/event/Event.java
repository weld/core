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

package javax.enterprise.event;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.TypeLiteral;

/**
 * An interface for firing events of a particular type, and registering
 * observers for events of that type.
 * 
 * @author Gavin King
 * @author Pete Muir
 * @author David Allen
 * 
 * @param <T>
 *            the type of the event object
 */

public interface Event<T>
{

   /**
    * Fire an event
    * 
    * @param event the event type
    */
   public void fire(T event);
   
   /**
    * Returns a child Event with the additional specified bindings.
    * @param bindings Additional bindings to add to child Event
    * @return new child Event
    */
   public Event<T> select(Annotation... bindings);
   
   /**
    * Returns a child Event of the type specified and additional specified bindings.
    * @param <U> The subtype of T for the child Event
    * @param subtype The class of the subtype of T
    * @param bindings Additional specified bindings
    * @return new child Event
    */
   public <U extends T> Event<U> select(Class<U> subtype, Annotation... bindings);
   
   /**
    * Returns a child Event of the type specified and additional specified bindings.
    * @param <U> The subtype of T for the child Event
    * @param subtype The TypeLiteral of the subtype of T
    * @param bindings Additional specified bindings
    * @return new child Event
    */   
   public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... bindings);
}
