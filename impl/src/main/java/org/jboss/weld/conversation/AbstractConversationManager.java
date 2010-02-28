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
import static org.jboss.weld.logging.messages.ConversationMessage.CLEANING_UP_CONVERSATION;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_LOCK_UNAVAILABLE;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_SWITCHED;
import static org.jboss.weld.logging.messages.ConversationMessage.CONVERSATION_TERMINATION_SCHEDULED;
import static org.jboss.weld.logging.messages.ConversationMessage.DESTROY_ALL_LRC;
import static org.jboss.weld.logging.messages.ConversationMessage.DESTROY_LRC;
import static org.jboss.weld.logging.messages.ConversationMessage.LRC_COUNT;
import static org.jboss.weld.logging.messages.ConversationMessage.NO_CONVERSATION_TO_RESTORE;
import static org.jboss.weld.logging.messages.ConversationMessage.UNABLE_TO_RESTORE_CONVERSATION;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.Conversation;
import javax.inject.Inject;

import org.jboss.weld.Container;
import org.jboss.weld.context.BusyConversationException;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.NonexistentConversationException;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.resources.spi.ScheduledExecutorServiceFactory;
import org.slf4j.cal10n.LocLogger;

/**
 * An abstract conversation manager
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractConversationManager implements ConversationManager, Serializable
{
   private static final long serialVersionUID = 1L;

   private static final LocLogger log = loggerFactory().getLogger(CONVERSATION);

   private boolean asynchronous = false;

   @Inject
   protected ConversationImpl conversation;

   @Inject
   private ConversationIdGenerator conversationIdGenerator;

   @Inject @ConversationConcurrentAccessTimeout
   private long concurrentAccessTimeout;

   private Map<String, ManagedConversation> managedConversations;

   public AbstractConversationManager()
   {
      managedConversations = new ConcurrentHashMap<String, ManagedConversation>();
   }

   public ConversationManager setAsynchronous(boolean asynchronous)
   {
      if (this.asynchronous == asynchronous)
      {
         return this;
      }
      if (!managedConversations.isEmpty())
      {
         log.warn("Switching modes with non-transient conversations present resets the timeouts");
      }
      if (asynchronous)
      {
         switchToAsynchronous();
      }
      else
      {
         switchToNonAsynchronous();
      }
      return this;
   }

   private void switchToNonAsynchronous()
   {
      for (ManagedConversation managedConversation : managedConversations.values())
      {
         managedConversation.cancelTermination();
         managedConversation.setTerminationHandle(null);
         managedConversation.touch();
      }
   }

   private void switchToAsynchronous()
   {
      for (ManagedConversation managedConversation : managedConversations.values())
      {
         managedConversation.setTerminationHandle(scheduleForTermination(managedConversation.getConversation()));
      }
   }

   private void destroyExpiredConversations()
   {
      for (Iterator<ManagedConversation> i = managedConversations.values().iterator(); i.hasNext();)
      {
         ManagedConversation managedConversation = i.next();
         if (managedConversation.isExpired())
         {
            managedConversation.destroy();
            i.remove();
         }
      }
   }

   public ConversationManager setupConversation(String cid)
   {
      if (!asynchronous)
      {
         destroyExpiredConversations();
      }
      if (cid == null)
      {
         log.trace(NO_CONVERSATION_TO_RESTORE);
         return this;
      }
      ManagedConversation resumedManagedConversation = managedConversations.get(cid);
      if (resumedManagedConversation == null)
      {
         throw new NonexistentConversationException(UNABLE_TO_RESTORE_CONVERSATION, cid, "id not known");
      }
      if (asynchronous && !resumedManagedConversation.cancelTermination())
      {
         throw new BusyConversationException(CONVERSATION_LOCK_UNAVAILABLE);
      }
      try
      {
         if (!resumedManagedConversation.lock(concurrentAccessTimeout))
         {
            throw new BusyConversationException(CONVERSATION_LOCK_UNAVAILABLE);
         }
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
         throw new BusyConversationException(CONVERSATION_LOCK_UNAVAILABLE);
      }
      String oldConversation = conversation.toString();
      conversation.switchTo(resumedManagedConversation.getConversation());
      getConversationContext().loadTransientBeanStore(getBeanStore(cid));
      log.trace(CONVERSATION_SWITCHED, oldConversation, conversation);
      return this;
   }

   private ConversationContext getConversationContext()
   {
      return Container.instance().services().get(ContextLifecycle.class).getConversationContext();
   }

   public ConversationManager teardownConversation()
   {
      log.trace(CLEANING_UP_CONVERSATION, conversation);
      if (conversation.isTransient())
      {
         endTransientConversation();
         if (conversation.getResumedId() != null)
         {
            handleResumedConversation();
         }
      }
      else
      {
         endNonTransientConversation();
      }
      return this;
   }

   private void handleResumedConversation()
   {
      ManagedConversation resumedConversation = managedConversations.remove(conversation.getResumedId());
      if (resumedConversation == null)
      {
         return;
      }
      resumedConversation.unlock();
      resumedConversation.destroy();
   }

   private void endNonTransientConversation()
   {
      getConversationContext().saveTransientBeanStore(getBeanStore(conversation.getId()));
      ManagedConversation oldManagedConversation = managedConversations.get(conversation.getId());
      if (oldManagedConversation != null)
      {
         oldManagedConversation.unlock();
         if (asynchronous)
         {
            oldManagedConversation.setTerminationHandle(scheduleForTermination(conversation));
         }
         else
         {
            oldManagedConversation.touch();
         }
      }
      else
      {
         ManagedConversation newManagedConversation = ManagedConversation.of(conversation.unProxy(this), getBeanStore(conversation.getId()));
         if (asynchronous)
         {
            log.trace(CONVERSATION_TERMINATION_SCHEDULED, conversation);
            newManagedConversation.setTerminationHandle(scheduleForTermination(conversation));
         }
         managedConversations.put(conversation.getId(), newManagedConversation);
      }
      log.trace(LRC_COUNT, managedConversations.size());
   }

   private void endTransientConversation()
   {
      getConversationContext().destroy();
      if (conversation.getResumedId() != null)
      {
         getBeanStore(conversation.getResumedId()).clear();
      }
   }

   /**
    * Creates a termination task for and schedules it
    * 
    * @param cid The id of the conversation to terminate
    * @return The asynchronous job handle
    */
   private Future<?> scheduleForTermination(Conversation conversation)
   {
      Runnable terminationTask = new TerminationTask(conversation.getId());
      return Container.instance().services().get(ScheduledExecutorServiceFactory.class).get().schedule(terminationTask, conversation.getTimeout(), TimeUnit.MILLISECONDS);
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
         log.debug(DESTROY_LRC, cid, "conversation timed out");
         ManagedConversation managedConversation = managedConversations.remove(cid);
         if (managedConversation != null)
         {
            managedConversation.destroy();
         }
         log.trace(LRC_COUNT, managedConversations.size());
      }
   }

   public ConversationManager destroyAllConversations()
   {
      log.debug(DESTROY_ALL_LRC, "session ended");
      log.trace(LRC_COUNT, managedConversations.size());
      for (ManagedConversation managedConversation : managedConversations.values())
      {
         log.debug(DESTROY_LRC, managedConversation, "session ended");
         managedConversation.destroy();
      }
      managedConversations.clear();
      return this;
   }

   public ConversationManager activateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).activateConversationContext();
      return this;
   }

   public ConversationManager deactivateContext()
   {
      Container.instance().services().get(ContextLifecycle.class).deactivateConversationContext();
      return this;
   }

   public String generateConversationId()
   {
      return conversationIdGenerator.nextId();
   }

   public Map<String, Conversation> getConversations()
   {
      Map<String, Conversation> conversations = new HashMap<String, Conversation>();
      for (ManagedConversation entry : managedConversations.values())
      {
         conversations.put(entry.getConversation().getId(), entry.getConversation());
      }
      return Collections.unmodifiableMap(conversations);
   }

   public boolean isContextActive()
   {
      return getConversationContext().isActive();
   }

   protected abstract BeanStore getBeanStore(String cid);

}
