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
import javax.context.SessionScoped;
import javax.inject.Current;
import javax.inject.Produces;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanMap;

@SessionScoped
public class DefaultConversationManager implements ConversationManager, Serializable
{
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   @Current
   private ConversationIdGenerator conversationIdGenerator;
   @Current
   private ConversationTerminator conversationTerminator;
   @Current
   private Conversation currentConversation;

   private Map<String, Future<?>> longRunningConversations;
   private HttpSession session;

   protected DefaultConversationManager()
   {
      log.trace("Created " + getClass());
      longRunningConversations = new ConcurrentHashMap<String, Future<?>>();
      session = null;
   }

   @Produces
   public Conversation produceNewTransientConversation()
   {
      Conversation conversation = ConversationImpl.of(conversationIdGenerator.nextId(), conversationTerminator.getTimeout());
      log.trace("Produced a new conversation: " + conversation);
      return conversation;
   }

   public void setSession(HttpSession session)
   {
      log.trace("Conversation manager got session " + session.getId());
      this.session = session;
   }

   public void beginConversation(String cid)
   {
      if (cid == null)
      {
         return;
      }
      if (!longRunningConversations.containsKey(cid))
      {
         log.info("Could not restore long-running conversation " + cid);
         return;
      }
      if (!longRunningConversations.remove(cid).cancel(false))
      {
         log.info("Failed to cancel termination of conversation " + cid);
      }
      else
      {
         ((ConversationImpl) currentConversation).become(cid, true, conversationTerminator.getTimeout());
      }
   }

   public void endConversation()
   {
      if (currentConversation.isLongRunning())
      {
         Runnable terminationTask = new TerminationTask(currentConversation.getId());
         Future<?> terminationHandle = conversationTerminator.scheduleForTermination(terminationTask);
         longRunningConversations.put(currentConversation.getId(), terminationHandle);
         log.trace("Scheduling " + currentConversation + " for termination");
      }
      else
      {
         log.trace("Destroying transient conversation " + currentConversation);
         ConversationContext.INSTANCE.destroy();
      }
   }

   private class TerminationTask implements Runnable
   {
      private String cid;

      public TerminationTask(String cid)
      {
         this.cid = cid;
      }

      public void run()
      {
         log.trace("Conversation " + cid + " timed out and was terminated");
         ConversationContext terminationContext = new ConversationContext();
         terminationContext.setBeanMap(new ConversationBeanMap(session, cid));
         terminationContext.destroy();
         longRunningConversations.remove(cid);
      }
   }

}
