/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.enterprise.context.Conversation;
import javax.enterprise.inject.Current;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * An abstract conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractConversationManager implements ConversationManager
{
   private static LogProvider log = Logging.getLogProvider(AbstractConversationManager.class);

   // The conversation terminator
   @Current
   private ConversationTerminator conversationTerminator;

   // The current conversation
   @Current
   private ConversationImpl currentConversation;

   // The conversation timeout in milliseconds waiting for access to a blocked
   // conversation
   @ConversationConcurrentAccessTimeout
   private long concurrentAccessTimeout;

   // A map of current active long-running conversation entries
   private Map<String, ConversationEntry> longRunningConversations;

   /**
    * Creates a new conversation manager
    */
   public AbstractConversationManager()
   {
      log.trace("Created " + getClass());
      longRunningConversations = new ConcurrentHashMap<String, ConversationEntry>();
   }

   public void beginOrRestoreConversation(String cid)
   {
      if (cid == null)
      {
         // No incoming conversation ID, nothing to do here, continue with
         // a transient conversation
         log.trace("No conversation id to restore");
         return;
      }
      if (!longRunningConversations.containsKey(cid))
      {
         // We got an incoming conversation ID but it was not in the map of
         // known ones, nothing to do. Log and return to continue with a
         // transient conversation
         log.warn("Could not restore long-running conversation " + cid);
         return;
      }
      ConversationEntry resumedConversationEntry = longRunningConversations.get(cid);
      // Try to get a lock to the requested conversation, log and return to
      // continue with a transient conversation if we fail
      try
      {
         if (!resumedConversationEntry.lock(concurrentAccessTimeout))
         {
            return;
         }
      }
      catch (InterruptedException e)
      {
         log.debug("Interrupted while trying to acquire lock");
         return;
      }
      // If we can't cancel the termination, release the lock, return and
      // continue
      // with a transient conversation
      if (!resumedConversationEntry.cancelTermination())
      {
         resumedConversationEntry.unlock();
      }
      else
      {
         // If all goes well, set the identity of the current conversation to
         // match the fetched long-running one
         String oldConversation = currentConversation.toString();
         currentConversation.switchTo(resumedConversationEntry.getConversation());
         log.trace("Conversation switched from " + oldConversation + " to " + currentConversation);
      }
   }

   // TODO: check that stuff gets terminated when you flip between several
   // long-running conversations
   public void cleanupConversation()
   {
      log.trace("Cleaning up conversation for " + currentConversation);
      String cid = currentConversation.getId();
      if (currentConversation.isLongRunning())
      {
         Future<?> terminationHandle = scheduleForTermination(cid, currentConversation.getTimeout());
         // When the conversation ends, a long-running conversation needs to
         // start its self-destruct. We can have the case where the conversation
         // is a previously known conversation (that had it's termination
         // canceled in the
         // beginConversation) or the case where we have a completely new
         // long-running conversation.
         ConversationEntry longRunningConversation = longRunningConversations.get(cid);
         if (longRunningConversation != null)
         {
            longRunningConversation.unlock();
            longRunningConversation.reScheduleTermination(terminationHandle);
         }
         else
         {
            ConversationEntry conversationEntry = ConversationEntry.of(getBeanStore(cid), currentConversation, terminationHandle);
            longRunningConversations.put(cid, conversationEntry);
         }
         log.trace("Scheduled " + currentConversation + " for termination, there are now " + longRunningConversations.size() + " long-running conversations");
      }
      else
      {
         // If the conversation is not long-running it can be a transient
         // conversation that has been so from the start or it can be a
         // long-running conversation that has been demoted during the request
         log.trace("Destroying transient conversation " + currentConversation);
         ConversationEntry longRunningConversation = longRunningConversations.remove(cid);
         if (longRunningConversation != null)
         {
            longRunningConversation.cancelTermination();
            longRunningConversation.unlock();
         }
         ConversationContext conversationContext = CurrentManager.rootManager().getServices().get(ConversationContext.class);
         conversationContext.destroy();
      }
      // If the conversation has been switched from one long
      // running-conversation to another with
      // Conversation.begin(String), we need to unlock the original conversation
      // and re-schedule
      // it for termination
      String originalCid = currentConversation.getOriginalCid();
      ConversationEntry longRunningConversation = originalCid == null ? null : longRunningConversations.get(originalCid);
      if (longRunningConversation != null)
      {
         longRunningConversation.unlock();
         longRunningConversation.reScheduleTermination(scheduleForTermination(originalCid, currentConversation.getTimeout()));
      }
   }

   /**
    * Creates a termination task for and schedules it
    * 
    * @param cid The id of the conversation to terminate
    * @return The asynchronous job handle
    */
   private Future<?> scheduleForTermination(String cid, long timeout)
   {
      Runnable terminationTask = new TerminationTask(cid);
      return conversationTerminator.scheduleForTermination(terminationTask, timeout);
   }

   /**
    * A termination task that destroys the conversation entry
    * 
    * @author Nicklas Karlsson
    * 
    */
   private class TerminationTask implements Runnable
   {
      // The conversation ID to terminate
      private String cid;

      /**
       * Creates a new termination task
       * 
       * @param cid The conversation ID
       */
      public TerminationTask(String cid)
      {
         this.cid = cid;
      }

      /**
       * Executes the termination
       */
      public void run()
      {
         log.debug("Conversation " + cid + " timed out. Destroying it");
         longRunningConversations.remove(cid).destroy();
         log.trace("There are now " + longRunningConversations.size() + " long-running conversations");
      }
   }

   public void destroyAllConversations()
   {
      log.debug("Destroying " + longRunningConversations.size() + " long-running conversations");
      for (ConversationEntry conversationEntry : longRunningConversations.values())
      {
         conversationEntry.destroy();
      }
      longRunningConversations.clear();
   }

   public Set<Conversation> getLongRunningConversations()
   {
      Set<Conversation> conversations = new HashSet<Conversation>();
      for (ConversationEntry conversationEntry : longRunningConversations.values())
      {
         conversations.add(conversationEntry.getConversation());
      }
      return Collections.unmodifiableSet(conversations);
   }
   
   public abstract BeanStore getBeanStore(String cid);

}
