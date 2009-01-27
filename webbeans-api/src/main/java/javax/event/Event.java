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

package javax.event;

import java.lang.annotation.Annotation;

/**
 * An interface for firing events of a particular type, and registering
 * observers for events of that type.
 * 
 * @author Gavin King
 * @author Pete Muir
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
    * @param bindings the event bindings
    */
   public void fire(T event, Annotation... bindings);
   
   /**
    * Register an observer for a specific type
    * 
    * @param observer the observer to register
    * @param bindings the bindings to observe the event for
    */
   public void observe(Observer<T> observer, Annotation... bindings);
   
}
