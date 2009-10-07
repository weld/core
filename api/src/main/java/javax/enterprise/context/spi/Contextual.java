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

import javax.enterprise.inject.CreationException;

/**
 * The contract between a context and a contextual type This interface should
 * not be implemented directly by the application.
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 */
public interface Contextual<T>
{
   /**
    * Create a new instance of the contextual type
    * 
    * @param creationalContext
    *           the creational context in which incompletely initialized
    *           contexts may be placed
    * @return the contextual instance
    * @throws CreationException
    *            if a checked exception occurs whilst creating the instance
    */
   public T create(CreationalContext<T> creationalContext);
   
   /**
    * Destroys an instance of the contexual type
    * 
    * @param instance
    *           the insance to destroy
    * @param creationalContext
    *           the creational context used to create the instance
    */
   public void destroy(T instance, CreationalContext<T> creationalContext);
}
