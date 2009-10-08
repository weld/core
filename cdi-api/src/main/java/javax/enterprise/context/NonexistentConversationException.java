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

package javax.enterprise.context;

/**
 * If the propagated conversation cannot be restored, the container must
 * associate the request with a new transient conversation and throw an
 * exception of type {@link NonexistentConversationException} from the
 * restore view phase of the JSF lifecycle. The application may handle this
 * exception using the JSF ExceptionHandler.
 * 
 * 
 * 
 * @author Pete Muir
 */

public class NonexistentConversationException extends ContextException
{

   private static final long serialVersionUID = -3599813072560026919L;

   public NonexistentConversationException()
   {
      super();
   }

   public NonexistentConversationException(String message)
   {
      super(message);
   }

   public NonexistentConversationException(Throwable cause)
   {
      super(cause);
   }

   public NonexistentConversationException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
