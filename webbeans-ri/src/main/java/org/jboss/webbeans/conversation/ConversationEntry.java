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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpSession;

import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanStore;

/**
 * Represents a long-running conversation entry
 * 
 * @author Nicklas Karlsson
 */
public class ConversationEntry
{
   private static LogProvider log = Logging.getLogProvider(ConversationEntry.class);

   // The conversation
   private ConversationImpl conversation;
   // The handle to the asynchronous timeout task
   private Future<?> terminationHandle;
   // The lock for concurrent access prevention
   private ReentrantLock concurrencyLock;

   /**
    * Creates a new conversation entry
    * 
    * @param cid The conversation ID
    * @param terminationHandle The timeout termination handle
    */
   protected ConversationEntry(ConversationImpl conversation, Future<?> terminationHandle)
   {
      // conversation is a proxy so we need to make a "real" instance
      this.conversation = new ConversationImpl(conversation);
      this.terminationHandle = terminationHandle;
      this.concurrencyLock = new ReentrantLock();
      log.trace("Created new conversation entry for conversation " + conversation);
   }

   /**
    * Factory method
    * 
    * @param cid The conversation ID
    * @param terminationHandle The timeout termination handle
    * @return A new conversation entry
    */
   public static ConversationEntry of(ConversationImpl conversation, Future<?> terminationHandle)
   {
      return new ConversationEntry(conversation, terminationHandle);
   }

   /**
    * Cancels the timeout termination
    * 
    * @return True if successful, false otherwise
    */
   public boolean cancelTermination()
   {
      boolean success = terminationHandle.cancel(false);
      if (success)
      {
         log.trace("Termination of conversation " + conversation + " cancelled");
      }
      else
      {
         log.warn("Failed to cancel termination of conversation " + conversation);
      }
      return success;
   }

   /**
    * Destroys the conversation and it's associated conversational context
    * 
    * @param session The HTTP session for the backing context bean store
    */
   public void destroy(HttpSession session)
   {
      log.debug("Destroying conversation " + conversation);
      if (!terminationHandle.isCancelled())
      {
         cancelTermination();
      }
      ConversationContext terminationContext = new ConversationContext();
      terminationContext.setBeanStore(new ConversationBeanStore(session, conversation.getId()));
      terminationContext.destroy();
      log.trace("Conversation " + conversation + " destroyed");
   }

   /**
    * Attempts to lock the conversation for exclusive usage
    * 
    * @param timeout The time in milliseconds to wait on the lock
    * @return True if lock was successful, false otherwise
    * @throws InterruptedException If the lock operation was unsuccessful
    */
   public boolean lock(long timeout) throws InterruptedException
   {
      boolean success = concurrencyLock.tryLock(timeout, TimeUnit.MILLISECONDS);
      if (success)
      {
         log.trace("Conversation " + conversation + " locked");
      }
      else
      {
         log.warn("Failed to lock conversation " + conversation + " in " + timeout + "ms");
      }
      return success;
   }

   /**
    * Attempts to unlock the conversation
    * 
    * @return true if the unlock was successful, false otherwise
    */
   public boolean unlock()
   {
      if (concurrencyLock.isHeldByCurrentThread())
      {
         concurrencyLock.unlock();
         log.trace("Unlocked conversation " + conversation);
      }
      else
      {
         log.warn("Unlock attempt by non-owner on conversation " + conversation);
      }
      return !concurrencyLock.isLocked();
   }

   /**
    * Re-schedules timeout termination
    * 
    * @param terminationHandle The fresh timeout termination handle
    */
   public void reScheduleTermination(Future<?> terminationHandle)
   {
      this.terminationHandle = terminationHandle;
      log.trace("Conversation " + conversation + " re-scheduled for termination");
   }

   public ConversationImpl getConversation()
   {
      return conversation;
   }
}
