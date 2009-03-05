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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.context.Conversation;
import javax.context.SessionScoped;
import javax.inject.Current;
import javax.inject.Produces;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.WebBean;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.conversation.bindings.ConversationConcurrentAccessTimeout;
import org.jboss.webbeans.conversation.bindings.ConversationIdName;
import org.jboss.webbeans.conversation.bindings.ConversationInactivityTimeout;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.HttpSessionManager;

/**
 * The default conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
@SessionScoped
@WebBean
public class ServletConversationManager implements ConversationManager, Serializable
{
   private static LogProvider log = Logging.getLogProvider(ServletConversationManager.class);

   private static final long CONVERSATION_TIMEOUT_IN_MS = 10 * 60 * 1000;
   private static final long CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS = 1 * 1000;
   private static final String CONVERSATION_ID_NAME = "cid";

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
   public ServletConversationManager()
   {
      log.trace("Created " + getClass());
      longRunningConversations = new ConcurrentHashMap<String, ConversationEntry>();
   }

   @Produces
   @ConversationInactivityTimeout
   @WebBean
   public static long getConversationTimeoutInMilliseconds()
   {
      log.trace("Produced conversation timeout " + CONVERSATION_TIMEOUT_IN_MS);
      return CONVERSATION_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationConcurrentAccessTimeout
   @WebBean
   public static long getConversationConcurrentAccessTimeout()
   {
      log.trace("Produced conversation concurrent access timeout " + CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS);
      return CONVERSATION_CONCURRENT_ACCESS_TIMEOUT_IN_MS;
   }

   @Produces
   @ConversationIdName
   @WebBean
   public static String getConversationIdName()
   {
      log.trace("Produced conversation id name " + CONVERSATION_ID_NAME);
      return CONVERSATION_ID_NAME;
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
         log.info("Could not restore long-running conversation " + cid);
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
         if (longRunningConversations.containsKey(currentConversation.getId()))
         {
            longRunningConversations.get(currentConversation.getId()).unlock();
            longRunningConversations.get(currentConversation.getId()).reScheduleTermination(terminationHandle);
         }
         else
         {
            ConversationEntry conversationEntry = ConversationEntry.of(currentConversation, terminationHandle);
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
         if (longRunningConversations.containsKey(cid))
         {
            ConversationEntry removedConversationEntry = longRunningConversations.remove(cid);
            if (removedConversationEntry != null)
            {
               removedConversationEntry.cancelTermination();
               removedConversationEntry.unlock();
            }
            else
            {
               log.debug("Failed to remove long-running conversation " + cid + " from list");
            }
         }
         ConversationContext.INSTANCE.destroy();
      }
      // If the conversation has been switched from one long running-conversation to another with 
      // Conversation.begin(String), we need to unlock the original conversation and re-schedule 
      // it for termination
      String originalCid = currentConversation.getOriginalCid();
      if (originalCid != null && longRunningConversations.containsKey(originalCid))
      {
         longRunningConversations.get(originalCid).unlock();
         longRunningConversations.get(originalCid).reScheduleTermination(scheduleForTermination(originalCid, currentConversation.getTimeout()));
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
      HttpSession session = CurrentManager.rootManager().getInstanceByType(HttpSessionManager.class).getSession();
      Runnable terminationTask = new TerminationTask(cid, session);
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
      private HttpSession session;

      /**
       * Creates a new termination task
       * 
       * @param cid The conversation ID
       */
      public TerminationTask(String cid, HttpSession session)
      {
         this.cid = cid;
         this.session = session;
      }

      /**
       * Executes the termination
       */
      public void run()
      {
         log.debug("Conversation " + cid + " timed out. Destroying it");
         longRunningConversations.remove(cid).destroy(session);
         log.trace("There are now " + longRunningConversations.size() + " long-running conversations");
      }
   }

   public void destroyAllConversations()
   {
      HttpSession session = CurrentManager.rootManager().getInstanceByType(HttpSessionManager.class).getSession();
      log.debug("Destroying " + longRunningConversations.size() + " long-running conversations in session " + session.getId());
      for (ConversationEntry conversationEntry : longRunningConversations.values())
      {
         conversationEntry.destroy(session);
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

}
