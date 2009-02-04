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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.context.Conversation;
import javax.context.RequestScoped;
import javax.context.SessionScoped;
import javax.inject.Current;
import javax.inject.Produces;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * The default conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
@SessionScoped
public class DefaultConversationManager implements ConversationManager, Serializable
{
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   // The conversation id generator
   @Current
   private ConversationIdGenerator conversationIdGenerator;

   // The conversation terminator
   @Current
   private ConversationTerminator conversationTerminator;

   // The current conversation
   @Current
   private Conversation currentConversation;

   // The current HTTP session
   @Current
   private HttpSession session;

   // A map of current active long-running conversation entries
   private Map<String, ConversationEntry> longRunningConversations;

   /**
    * Creates a new conversation manager
    */
   public DefaultConversationManager()
   {
      log.trace("Created " + getClass());
      longRunningConversations = new ConcurrentHashMap<String, ConversationEntry>();
   }

   /**
    * Producer method for transient conversations
    * 
    * @return A new transient conversation
    */
   @Produces
   @RequestScoped
   public Conversation produceNewTransientConversation()
   {
      Conversation conversation = ConversationImpl.of(conversationIdGenerator.nextId(), getInactivityTimeoutInMilliseconds());
      log.trace("Produced a new conversation: " + conversation);
      return conversation;
   }

   public void beginConversation(String cid)
   {
      if (cid == null)
      {
         // No incoming conversation ID, nothing to do here, Conversation
         // factory will produce new transient conversation
         return;
      }
      if (!longRunningConversations.containsKey(cid))
      {
         // We got an incoming conversation ID but it was not in the map of
         // known ones, nothing to do, factory to the rescue
         log.info("Could not restore long-running conversation " + cid);
         return;
      }
      // Try to get a lock to the requested conversation, return and fall back
      // to new transient conversation from factory
      // if we fail
      try
      {
         if (!longRunningConversations.get(cid).lock(getConcurrentAccessTimeoutInMilliseconds()))
         {
            log.info("Could not acquire conversation lock in " + getConcurrentAccessTimeoutInMilliseconds() + "ms, giving up");
            return;
         }
      }
      catch (InterruptedException e)
      {
         log.info("Interrupted while trying to acquire lock");
         return;
      }
      // If we can't cancel the termination, release the lock, return and fall
      // back to new transient conversation from factory
      if (!longRunningConversations.get(cid).cancelTermination())
      {
         longRunningConversations.get(cid).unlock();
         log.info("Failed to cancel termination of conversation " + cid);
      }
      else
      {
         // If all goes well, set the identity of the current conversation to
         // match the fetched long-running one
         ((ConversationImpl) currentConversation).become(cid, true, getInactivityTimeoutInMilliseconds());
      }
   }

   public void endConversation()
   {
      String cid = currentConversation.getId();
      if (currentConversation.isLongRunning())
      {
         Future<?> terminationHandle = scheduleForTermination(cid);
         // When the conversation ends, a long-running conversation needs to
         // start its self-destruct. We can have the case where the conversation
         // is a previously known conversation (that had it's termination canceled in the
         // beginConversation) or the case where we have a completely new long-running conversation.
         if (longRunningConversations.containsKey(currentConversation.getId()))
         {
            longRunningConversations.get(currentConversation.getId()).unlock();
            longRunningConversations.get(currentConversation.getId()).reScheduleTermination(terminationHandle);
         }
         else
         {
            ConversationEntry conversationEntry = ConversationEntry.of(cid, terminationHandle);
            longRunningConversations.put(cid, conversationEntry);
         }
         log.trace("Scheduling " + currentConversation + " for termination");
      }
      else
      {
         // If the conversation is not long-running it can be a transient
         // conversation that has been so from the start or it can be a
         // long-running conversation that has been demoted during the request
         log.trace("Destroying transient conversation " + currentConversation);
         if (longRunningConversations.containsKey(cid))
         {
            if (!longRunningConversations.get(cid).cancelTermination())
            {
               log.info("Failed to cancel termination of conversation " + cid);
            }
            longRunningConversations.get(cid).unlock();
         }
         ConversationContext.INSTANCE.destroy();
      }
   }

   private Future<?> scheduleForTermination(String cid)
   {
      Runnable terminationTask = new TerminationTask(cid);
      return conversationTerminator.scheduleForTermination(terminationTask, getInactivityTimeoutInMilliseconds());
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
         log.trace("Conversation " + cid + " timed out and was terminated");
         longRunningConversations.remove(cid).destroy(session);
      }
   }

   public void destroyAllConversations()
   {
      for (ConversationEntry conversationEntry : longRunningConversations.values())
      {
         conversationEntry.destroy(session);
      }
      longRunningConversations.clear();
   }

   public long getConcurrentAccessTimeoutInMilliseconds()
   {
      return 1000;
   }

   public long getInactivityTimeoutInMilliseconds()
   {
      return 10 * 60 * 1000;
   }

}
