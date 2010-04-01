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
import static org.jboss.weld.logging.messages.ConversationMessage.BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION;
import static org.jboss.weld.logging.messages.ConversationMessage.DEMOTED_LRC;
import static org.jboss.weld.logging.messages.ConversationMessage.END_CALLED_ON_TRANSIENT_CONVERSATION;
import static org.jboss.weld.logging.messages.ConversationMessage.PROMOTED_TRANSIENT;
import static org.jboss.weld.logging.messages.ConversationMessage.SWITCHED_CONVERSATION;

import java.io.Serializable;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.weld.exceptions.ForbiddenStateException;
import org.slf4j.cal10n.LocLogger;

/**
 * The current conversation implementation
 * 
 * @author Nicklas Karlsson
 * @see javax.enterprise.context.Conversation
 */
@RequestScoped
@Named("javax.enterprise.context.conversation")
@Default
public class ConversationImpl implements Conversation, Serializable
{
   private static final long serialVersionUID = 1L;

   private static final LocLogger log = loggerFactory().getLogger(CONVERSATION);

   @Inject
   private ConversationManager2 conversationManager;

   private String id;
   private boolean _transient = true;
   private long timeout;
   private String resumedId;

   /**
    * Creates a new conversation
    */
   public ConversationImpl()
   {
   }

   private void checkForActiveConversationContext(String when)
   {
      if (!conversationManager.isContextActive())
      {
         throw new ContextNotActiveException("Conversation context not active when calling " + when + " on " + this);
      }
   }

   /**
    * Creates a new conversation from an existing one.
    * 
    * @param conversation The old conversation
    */
   protected ConversationImpl(Conversation conversation, ConversationManager2 conversationManager)
   {
      id = conversation.getId();
      _transient = conversation.isTransient();
      timeout = conversation.getTimeout();
      // manual injection because of new() usage for unProxy();
      this.conversationManager = conversationManager;
   }
   
   public static ConversationImpl of(Conversation conversation, ConversationManager2 conversationManager)
   {
      return new ConversationImpl(conversation, conversationManager);
   }

   /**
    * Initializes a new conversation. The timeout value is only applied if
    * the local value is currently unset (0).
    * 
    * @param conversationIdGenerator The conversation ID generator
    * @param timeout The conversation inactivity timeout
    */
   @Inject
   public void init(@ConversationInactivityTimeout long timeout)
   {
      if (this.timeout == 0) 
      {
         this.timeout = timeout;
      }
      _transient = true;
   }

   public void begin()
   {
      checkForActiveConversationContext("Conversation.begin()");
      if (!_transient)
      {
         throw new ForbiddenStateException(BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION);
      }
      log.debug(PROMOTED_TRANSIENT, id);
      _transient = false;
      id = conversationManager.generateConversationId();
   }

   public void begin(String id)
   {
      checkForActiveConversationContext("Conversation.begin(String)");
      if (!_transient)
      {
         throw new ForbiddenStateException(BEGIN_CALLED_ON_LONG_RUNNING_CONVERSATION);
      }
      if (conversationManager.getConversations().containsKey(id))
      {
         throw new IllegalStateException("Conversation ID " + id + " is already in use");
      }
      _transient = false;
      this.id = id;
   }

   public void end()
   {
      checkForActiveConversationContext("Conversation.end()");
      if (_transient)
      {
         throw new ForbiddenStateException(END_CALLED_ON_TRANSIENT_CONVERSATION);
      }
      log.debug(DEMOTED_LRC, id);
      _transient = true;
      id = null;
   }

   public String getId()
   {
      checkForActiveConversationContext("Conversation.getId()");
      return id;
   }

   public long getTimeout()
   {
      checkForActiveConversationContext("Conversation.getTimeout()");
      return timeout;
   }

   public void setTimeout(long timeout)
   {
      checkForActiveConversationContext("Conversation.setTimeout()");
      this.timeout = timeout;
   }

   /**
    * Assumes the identity of another conversation
    * 
    * @param conversation The new conversation
    * 
    */
   public void switchTo(Conversation conversation)
   {
      log.debug(SWITCHED_CONVERSATION, this, conversation);
      id = conversation.getId();
      _transient = conversation.isTransient();
      timeout = conversation.getTimeout();
      this.resumedId = id;
   }

   @Override
   public String toString()
   {
      return "ID: " + id + ", transient: " + _transient + ", timeout: " + timeout + "ms";
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof ConversationImpl)
      {
         ConversationImpl that = (ConversationImpl) obj;
         return (id == null || that.getId() == null) ? false : id.equals(that.getId());
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
      checkForActiveConversationContext("Conversation.isTransient()");
      return _transient;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getResumedId()
   {
      return resumedId;
   }

   public ConversationImpl unProxy(ConversationManager2 conversationManager)
   {
      return new ConversationImpl(this, conversationManager);
   }
}
