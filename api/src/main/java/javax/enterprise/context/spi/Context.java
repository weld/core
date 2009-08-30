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

package javax.enterprise.context.spi;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;

/**
 * The contract between the manager and a contextual object.
 * This interface should not be called directly by the application.
 * 
 * @author Gavin King
 * @author Pete Muir
 */

public interface Context
{

   /**
    * The scope which this context implements
    * 
    * @return the scope
    */
   public Class<? extends Annotation> getScope();

   /**
    * Return an existing instance of a contextual type or create a new instance
    * of a contextual type
    * 
    * @param <T> the type of contextual type
    * @param contextual the contextual type
    * @param creationalContext the creational context in which incompletely
    *                          initialized instances may be placed
    * @return the contextual instance, or null if no creational context is given
    *         and an instance does not exist in the context
    * @throws ContextNotActiveException if the context is not active
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext);
   
   /**
    * Return an existing instance of a contextual type or create a new instance
    * of a contextual type
    * 
    * @param <T> the type of the contextual type
    * @param contextual the contextual type
    * @return the contextual instance, or null if an instance does not exist in
    *         the context
    * @throws ContextNotActiveException if the context is not active  
    */
   public <T> T get(Contextual<T> contextual);

   /**
    * The context is only active at certain points in the application lifecycle
    * 
    * @return true if the context is active
    */
   boolean isActive();

}
