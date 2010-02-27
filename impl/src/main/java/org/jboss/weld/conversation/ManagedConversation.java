/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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
public class ManagedConversation
{
   private static final LocLogger log = loggerFactory().getLogger(CONVERSATION);

   private ConversationImpl conversation;
   private Future<?> terminationHandle;
   private ReentrantLock concurrencyLock;
   private BeanStore beanStore;
   private long touched;

   /**
    * Creates a new conversation entry
    * 
    * @param cid The conversation ID
    * @param terminationHandle The timeout termination handle
    */
   protected ManagedConversation(ConversationImpl conversation, BeanStore beanStore, Future<?> terminationHandle)
   {
      this.conversation = conversation;
      this.beanStore = beanStore;
      this.terminationHandle = terminationHandle;
      this.concurrencyLock = new ReentrantLock();
      touch();
   }

   /**
    * Static factory method
    * 
    * @param conversation The conversation to manager
    * @param beanStore The beanstore of the conversation for termination
    * @param terminationHandle The asynchronous termination handle
    * 
    * @return A manager conversation
    */
   public static ManagedConversation of(ConversationImpl conversation, BeanStore beanStore, Future<?> terminationHandle)
   {
      return new ManagedConversation(conversation, beanStore, terminationHandle);
   }

   /**
    * Static factory method
    * 
    * @param conversation The conversation to manager
    * @param beanStore The beanstore of the conversation for termination
    * 
    * @return A manager conversation
    */
   public static ManagedConversation of(ConversationImpl conversation, BeanStore beanStore)
   {
      return of(conversation, beanStore, null);
   }      

   /**
    * Cancels the asynchronous termination
    * 
    * @return True if successful, false otherwise
    */
   public boolean cancelTermination()
   {
      if (terminationHandle == null)
      {
         return false;
      }
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
      // FIXME: Not really needed but there is a small window for overlapping if it fires(?)
      if (terminationHandle != null && !terminationHandle.isCancelled())
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
    * Sets the asynchronous termination handle
    * 
    * @param terminationHandle The termination handle
    */
   public void setTerminationHandle(Future<?> terminationHandle)
   {
      this.terminationHandle = terminationHandle;
   }

   /**
    * Gets the wrapped conversation
    * 
    * @return The conversation
    */
   public ConversationImpl getConversation()
   {
      return conversation;
   }
   
   /**
    * Checks if the conversation has expired
    * 
    * @return true if expired, false otherwise
    */
   public boolean isExpired()
   {
      return System.currentTimeMillis() > (touched + conversation.getTimeout());
   }
   
   /**
    * Touches the managed conversation, updating the "last used" timestamp
    */
   public void touch() 
   {
      touched = System.currentTimeMillis();
   }

}
