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
package org.jboss.webbeans;

/**
 * Thrown if an injection point of primitive type resolves to a bean which may
 * be null
 * 
 * @author Pete Muir
 */
public class NullableDependencyException extends DeploymentException
{

   private static final long serialVersionUID = 6877485218767005761L;

   public NullableDependencyException()
   {
      super();
   }

   public NullableDependencyException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public NullableDependencyException(String message)
   {
      super(message);
   }

   public NullableDependencyException(Throwable throwable)
   {
      super(throwable);
   }

   

}
