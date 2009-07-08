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

package javax.enterprise.inject.spi;

/**
 * The container fires an event of this type for each observer method
 * that is registered.
 * 
 * @author Gavin King
 * @author David Allen
 *
 * @param <X> The bean type containing the observer method
 * @param <T> The type of the event being observed
 */
public interface ProcessObserverMethod<X, T>
{
   /**
    * The AnnotatedMethod representing the observer method
    * @return the AnnotatedMethod representing the observer method 
    */
   public AnnotatedMethod<X> getAnnotatedMethod();

   /**
    * The ObserverMethod object that will be used by the container
    * to invoke the observer when a matching event is fired.
    * @return the ObserverMethod object that will be used by the container to call the observer method
    */
   public ObserverMethod<X, T> getObserverMethod();

   /**
    * Registers a definition error with the container, causing the container to
    * abort deployment after bean discovery is complete.
    * 
    * @param t A throwable representing the definition error
    */
   public void addDefinitionError(Throwable t);
}
