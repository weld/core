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
 * The container ensures that a long-running conversation may be associated with
 * at most one request at a time, by blocking or rejecting concurrent requests.
 * If the container rejects a request, it must associate the request with a new
 * transient conversation and throw an exception of type
 * {@link BusyConversationException} from the restore view phase of the JSF
 * lifecycle.
 * 
 * 
 * @author Pete Muir
 */

public class BusyConversationException extends ContextException
{

   private static final long serialVersionUID = -3599813072560026919L;

   public BusyConversationException()
   {
      super();
   }

   public BusyConversationException(String message)
   {
      super(message);
   }

   public BusyConversationException(Throwable cause)
   {
      super(cause);
   }

   public BusyConversationException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
