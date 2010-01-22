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
package org.jboss.weld.conversation;

import static org.jboss.weld.logging.Category.CONVERSATION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_LOCKED;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_TERMINATION_CANCELLATION_FAILED;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_TERMINATION_CANCELLED;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_UNAVAILBLE;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_UNLOCKED;
import static org.jboss.weld.logging.messages.ConversationMessage.ILLEGAL_CONVERSATION_UNLOCK_ATTEMPT;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.api.BeanStore;
import org.slf4j.cal10n.LocLogger;

/**
 * Represents a long-running conversation entry
 * 
 * @author Nicklas Karlsson
 */
public class ConversationEntry
{
   private static final LocLogger log = loggerFactory().getLogger(CONVERSATION);

   // The conversation
   private ConversationImpl conversation;
   // The handle to the asynchronous timeout task
   private Future<?> terminationHandle;
   // The lock for concurrent access prevention
   private ReentrantLock concurrencyLock;
   // The Bean Store of the conversations
   private BeanStore beanStore;

   /**
    * Creates a new conversation entry
    * 
    * @param cid The conversation ID
    * @param terminationHandle The timeout termination handle
    */
   protected ConversationEntry(BeanStore beanStore, ConversationImpl conversation, Future<?> terminationHandle)
   {
      this.beanStore = beanStore;
      // conversation is a proxy so we need to make a "real" instance
      this.conversation = new ConversationImpl(conversation);
      this.terminationHandle = terminationHandle;
      this.concurrencyLock = new ReentrantLock();
   }

   /**
    * Factory method
    * 
    * @param cid The conversation ID
    * @param terminationHandle The timeout termination handle
    * @return A new conversation entry
    */
   public static ConversationEntry of(BeanStore beanStore, ConversationImpl conversation, Future<?> terminationHandle)
   {
      return new ConversationEntry(beanStore, conversation, terminationHandle);
   }

   /**
    * Cancels the timeout termination
    * 
    * @return True if successful, false otherwise
    */
   public boolean cancelTermination()
   {
      if (terminationHandle.isCancelled())
      {
         return true;
      }
      boolean success = terminationHandle.cancel(false);
      if (success)
      {
         log.trace(CONVERSATION_TERMINATION_CANCELLED, conversation);
      }
      else
      {
         log.warn(CONVERSATION_TERMINATION_CANCELLATION_FAILED, conversation);
      }
      return success;
   }

   /**
    * Destroys the conversation and it's associated conversational context
    */
   public void destroy()
   {
      if (!terminationHandle.isCancelled())
      {
         cancelTermination();
      }
      ConversationContext terminationContext = new ConversationContext();
      terminationContext.setBeanStore(beanStore);
      terminationContext.destroy();
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
         log.trace(CONVERSATION_LOCKED, conversation);
      }
      else
      {
         log.warn(CONVERSATION_UNAVAILBLE, timeout, conversation);
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
      if (!concurrencyLock.isLocked())
      {
         return true;
      }
      if (concurrencyLock.isHeldByCurrentThread())
      {
         concurrencyLock.unlock();
         log.trace(CONVERSATION_UNLOCKED, conversation);
      }
      else
      {
         log.warn(ILLEGAL_CONVERSATION_UNLOCK_ATTEMPT, conversation, "not owner");
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
   }

   public ConversationImpl getConversation()
   {
      return conversation;
   }
}
