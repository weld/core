/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.xml;

import javax.enterprise.inject.InjectionException;

import org.jboss.weld.exceptions.WeldExceptionMessage;

/**
 * Used for exceptions from the Weld XML parser and provides localization
 * support.
 * 
 * @author David Allen
 */
public class WeldXmlException extends InjectionException
{

   private static final long    serialVersionUID = 2L;

   private WeldExceptionMessage message;

   public WeldXmlException(Throwable throwable)
   {
      super(throwable);
      message = new WeldExceptionMessage(throwable.getLocalizedMessage());
   }

   public <E extends Enum<?>> WeldXmlException(E key, Object... args)
   {
      message = new WeldExceptionMessage(key, args);
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
