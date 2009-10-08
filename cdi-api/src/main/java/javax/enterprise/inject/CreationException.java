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

package javax.enterprise.inject;


/**
 * Wraps any checked exceptions which occur during creation of an bean
 * 
 * @author Pete Muir
 */
public class CreationException extends InjectionException
{

   private static final long serialVersionUID = 1002854668862145298L;

   public CreationException()
   {
      
   }

   public CreationException(String message)
   {
      super(message);
   }

   public CreationException(Throwable cause)
   {
      super(cause);
   }

   public CreationException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
