/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.exceptions;

/**
 * Provides message localization service for the
 * {@link javax.enterprise.inject.UnproxyableResolutionException}.
 * 
 * @author David Allen
 */
public class UnproxyableResolutionException extends javax.enterprise.inject.UnproxyableResolutionException
{
   private static final long    serialVersionUID = 2L;

   private WeldExceptionMessage message;

   /**
    * Creates a new exception with the given cause.
    * 
    * @param throwable The cause of the exception
    */
   public UnproxyableResolutionException(Throwable throwable)
   {
      super(throwable);
      message = new WeldExceptionStringMessage(throwable.getLocalizedMessage());
   }

   /**
    * Creates a new exception with the given localized message key and optional
    * arguments for the message.
    * 
    * @param <E> The enumeration type for the message keys
    * @param key The localized message to use
    * @param args Optional arguments to insert into the message
    */
   public <E extends Enum<?>> UnproxyableResolutionException(E key, Object... args)
   {
      message = new WeldExceptionKeyMessage(key, args);
   }

   @Override
   public String getLocalizedMessage()
   {
      return getMessage();
   }

   @Override
   public String getMessage()
   {
      return message.getAsString();
   }

}
