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
package org.jboss.webbeans.conversation;

import javax.annotation.Named;
import javax.context.Conversation;
import javax.context.RequestScoped;
import javax.inject.Initializer;
import javax.inject.Standard;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * The current conversation implementation
 * 
 * @author Nicklas Karlsson
 * @see javax.context.Conversation
 */
@RequestScoped
@Named("javax.context.conversation")
@Standard
public class ConversationImpl implements Conversation
{

   private static LogProvider log = Logging.getLogProvider(ConversationImpl.class);

   // The conversation ID
   private String cid;
   // The original conversation ID (if any)
   private String originalCid;
   // Is the conversation long-running?
   private boolean longRunning;
   // The timeout in milliseconds
   private long timeout;

   /**
    * Creates a new conversation
    */
   public ConversationImpl()
   {
   }

   /**
    * Creates a new conversation from an existing one.
    *  
    * @param conversation The old conversation
    */
   public ConversationImpl(Conversation conversation)
   {
      this.cid = conversation.getId();
      this.longRunning = conversation.isLongRunning();
      this.timeout = conversation.getTimeout();
   }

   /**
    * Initializes a new conversation
    * 
    * @param conversationIdGenerator The conversation ID generator
    * @param timeout The conversation inactivity timeout
    */
   @Initializer
   public void init(ConversationIdGenerator conversationIdGenerator, @ConversationInactivityTimeout long timeout)
   {
      this.cid = conversationIdGenerator.nextId();
      this.timeout = timeout;
      this.longRunning = false;
      log.debug("Created a new conversation " + this);
   }

   public void begin()
   {
      log.debug("Promoted conversation " + cid + " to long-running");
      longRunning = true;
   }

   public void begin(String id)
   {
      // Store away the (first) change to the conversation ID. If the original conversation was long-running,
      // we might have to place it back for termination once the request is over.
      if (originalCid == null)
      {
         originalCid = cid;
      }
      cid = id;
      begin();
   }

   public void end()
   {
      log.debug("Demoted conversation " + cid + " to transient");
      longRunning = false;
   }

   public String getId()
   {
      return cid;
   }

   public long getTimeout()
   {
      return timeout;
   }

   public boolean isLongRunning()
   {
      return longRunning;
   }

   public void setTimeout(long timeout)
   {
      this.timeout = timeout;
   }

   /**
    * Assumes the identity of another conversation
    * 
    * @param conversation The new conversation
    * 
    */
   public void switchTo(Conversation conversation)
   {
      log.debug("Switched conversation from " + this);
      cid = conversation.getId();
      longRunning = conversation.isLongRunning();
      timeout = conversation.getTimeout();
      log.debug(" to " + this);
   }

   @Override
   public String toString()
   {
      return "ID: " + cid + ", long-running: " + longRunning + ", timeout: " + timeout + "ms";
   }

   public void setLongRunning(boolean longRunning)
   {
      log.debug("Set conversation " + cid + " to long-running: " + longRunning);
      this.longRunning = longRunning;
   }

   /**
    * Gets the original ID of the conversation
    * 
    * @return The id
    */
   public String getOriginalCid()
   {
      return originalCid;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      
      if (obj == null || !(obj instanceof Conversation))
         return false;
      String otherCid = ((Conversation)obj).getId();
      return (cid == null || otherCid == null) ? false : cid.equals(otherCid);
   }
   
   @Override
   public int hashCode()
   {
      return cid == null ? super.hashCode() : cid.hashCode();
   }
}
