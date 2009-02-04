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
import org.jboss.webbeans.servlet.ConversationBeanMap;

/**
 * Represents a long-running conversation entry
 * 
 * @author Nicklas Karlsson
 */
public class ConversationEntry
{
   // The conversation ID
   private String cid;
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
   protected ConversationEntry(String cid, Future<?> terminationHandle)
   {
      this.cid = cid;
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
   public static ConversationEntry of(String cid, Future<?> terminationHandle)
   {
      return new ConversationEntry(cid, terminationHandle);
   }

   /**
    * Cancels the timeout termination
    * 
    * @return True if successful, false otherwise
    */
   public boolean cancelTermination()
   {
      return terminationHandle.cancel(false);
   }

   /**
    * Destroys the conversation and it's associated conversational context 
    * 
    * @param session The HTTP session for the backing context beanmap
    */
   public void destroy(HttpSession session)
   {
      if (!terminationHandle.isCancelled())
      {
         cancelTermination();
      }
      ConversationContext terminationContext = new ConversationContext();
      terminationContext.setBeanMap(new ConversationBeanMap(session, cid));
      terminationContext.destroy();
   }

   /**
    * Attempts to lock the conversation for exclusive usage 
    * 
    * @param timeoutInMilliseconds The time in milliseconds to wait on the lock
    * @return True if lock was successful, false otherwise
    * @throws InterruptedException If the lock operation was unsuccessful
    */
   public boolean lock(long timeoutInMilliseconds) throws InterruptedException
   {
      return concurrencyLock.tryLock(timeoutInMilliseconds, TimeUnit.MILLISECONDS);
   }

   /**
    * Attempts to unlock the conversation
    */
   public void unlock()
   {
      concurrencyLock.unlock();
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

}
