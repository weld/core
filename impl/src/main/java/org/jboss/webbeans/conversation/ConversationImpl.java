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

import java.io.Serializable;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Named;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * The current conversation implementation
 * 
 * @author Nicklas Karlsson
 * @see javax.enterprise.context.Conversation
 */
@RequestScoped
@Named("javax.enterprise.context.conversation")
public class ConversationImpl implements Conversation, Serializable
{

   /**
    * Eclipse generated UID.
    */
   private static final long serialVersionUID = 5262382965141841363L;

   private static LogProvider log = Logging.getLogProvider(ConversationImpl.class);

   // The conversation ID
   private String id;
   // The original conversation ID (if any)
   private String originalId;
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
   public ConversationImpl(ConversationImpl conversation)
   {
      this.id = conversation.getUnderlyingId();
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
      this.id = conversationIdGenerator.nextId();
      this.timeout = timeout;
      this.longRunning = false;
      log.debug("Created a new conversation " + this);
   }

   public void begin()
   {
      if (isLongRunning())
      {
         throw new IllegalStateException("Attempt to call begin() on a long-running conversation");
      }
      log.debug("Promoted conversation " + id + " to long-running");
      longRunning = true;
   }

   public void begin(String id)
   {
      // Store away the (first) change to the conversation ID. If the original
      // conversation was long-running,
      // we might have to place it back for termination once the request is
      // over.
      if (originalId == null)
      {
         originalId = id;
      }
      this.id = id;
      begin();
   }

   public void end()
   {
      if (!isLongRunning())
      {
         throw new IllegalStateException("Attempt to call end() on a transient conversation");
      }
      log.debug("Demoted conversation " + id + " to transient");
      this.longRunning = false;
   }

   public String getId()
   {
      if (isLongRunning())
      {
         return id;
      }
      else
      {
         return null;
      }
   }

   /**
    * Get the Conversation Id, regardless of whether the conversation is long
    * running or transient, needed for internal operations
    * 
    * @return the id
    */
   public String getUnderlyingId()
   {
      return id;
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
   public void switchTo(ConversationImpl conversation)
   {
      log.debug("Switched conversation from " + this);
      id = conversation.getUnderlyingId();
      longRunning = conversation.isLongRunning();
      timeout = conversation.getTimeout();
      log.debug(" to " + this);
   }

   @Override
   public String toString()
   {
      return "ID: " + id + ", long-running: " + longRunning + ", timeout: " + timeout + "ms";
   }

   public void setLongRunning(boolean longRunning)
   {
      log.debug("Set conversation " + id + " to long-running: " + longRunning);
      this.longRunning = longRunning;
   }

   /**
    * Gets the original ID of the conversation
    * 
    * @return The id
    */
   public String getOriginalId()
   {
      return originalId;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ConversationImpl)
      {
         ConversationImpl that = (ConversationImpl) obj;
         return (id == null || that.getUnderlyingId() == null) ? false : id.equals(that.getUnderlyingId());
      }
      else
      {
         return false;
      }
   }

   @Override
   public int hashCode()
   {
      return id == null ? super.hashCode() : id.hashCode();
   }

   public boolean isTransient()
   {
      return !isLongRunning();
   }
}
