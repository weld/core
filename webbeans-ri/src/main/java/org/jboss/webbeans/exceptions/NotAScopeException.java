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

package org.jboss.webbeans.exceptions;

import javax.webbeans.DefinitionException;

/**
 * Exception for incorrect scope usage
 * 
 * @author Pete Muir
 */
public class NotAScopeException extends DefinitionException
{
   private static final long serialVersionUID = 1L;

   /**
    * Constructor
    */
   public NotAScopeException()
   {
      super();
   }

   /**
    * Constructor
    * 
    * @param message The exception message
    * @param throwable The root exception
    */
   public NotAScopeException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   /**
    * Constructor
    * 
    * @param message The exception message
    */
   public NotAScopeException(String message)
   {
      super(message);
   }

   /**
    * Constructor
    * 
    * @param throwable The root exception
    */
   public NotAScopeException(Throwable throwable)
   {
      super(throwable);
   }

}
