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

package javax.context;

import javax.inject.ExecutionException;

/**
 * A ContextNotActiveException is thrown if at a particular point in the
 * execution of the program the scope is inactive with respect to the current
 * thread. When the scope is inactive, any invocation of the get() from the
 * current thread upon the Context object for that scope results in a
 * ContextNotActiveException.
 * 
 * @author Pete Muir
 * @author Shane Bryzak
 */

public class ContextNotActiveException extends ExecutionException
{
   
   private static final long serialVersionUID = -3599813072560026919L;

   public ContextNotActiveException()
   {
      super();
   }
   
   public ContextNotActiveException(String message)
   {
      super(message);
   }
   
   public ContextNotActiveException(Throwable cause)
   {
      super(cause);
   }
   
   public ContextNotActiveException(String message, Throwable cause)
   {
      super(message, cause);
   }
   
}
