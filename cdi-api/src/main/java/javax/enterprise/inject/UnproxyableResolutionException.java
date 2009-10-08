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
 * Thrown if a bean has normal scope and the type cannot be proxied by the 
 * container
 * 
 * @author Pete Muir
 */
public class UnproxyableResolutionException extends ResolutionException
{

   private static final long serialVersionUID = 1667539354548135465L;

   public UnproxyableResolutionException()
   {
      super();
   }

   public UnproxyableResolutionException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public UnproxyableResolutionException(String message)
   {
      super(message);
   }

   public UnproxyableResolutionException(Throwable throwable)
   {
      super(throwable);
   }

   

}
