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
package org.jboss.weld.conversation;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.SessionScoped;

import org.jboss.weld.log.LogProvider;
import org.jboss.weld.log.Logging;

/**
 * A ConversationIdGenerator implementation using running numerical values
 *  
 * @author Nicklas Karlsson
 *
 */
@SessionScoped

public class NumericConversationIdGenerator implements ConversationIdGenerator, Serializable
{
   private static final long serialVersionUID = -587408626962044442L;

   private static LogProvider log = Logging.getLogProvider(NumericConversationIdGenerator.class);
   // The next conversation ID
   private AtomicInteger id;

   /**
    * Creates a new conversation ID generator
    */
   public NumericConversationIdGenerator()
   {
      id = new AtomicInteger(1);
   }

   public String nextId()
   {
      int nextId = id.getAndIncrement();
      log.trace("Generated new conversation id " + nextId);
      return String.valueOf(nextId);
   }

}
