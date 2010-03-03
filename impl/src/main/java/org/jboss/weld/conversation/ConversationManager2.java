package org.jboss.weld.conversation;
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

import java.util.Map;

import javax.enterprise.context.Conversation;

public interface ConversationManager2
{
   /**
    * Checks the state of the conversation context
    * 
    * @return true if the conversation context is active, false otherwise
    */
   public abstract boolean isContextActive();
   
   /**
    * Sets up and activates the conversation context
    * 
    * @return The conversation manager
    * 
    * @throws IllegalStateException if the context is already active
    */   
   public abstract ConversationManager2 setupContext();
   
   /**
    * Destroys the conversations and deactivates the conversation context
    * 
    * @return The conversation manager
    * 
    * @throws IllegalStateException if the context is already deactive
    */   
   public abstract ConversationManager2 teardownContext();

   /**
    * Resumes a long running conversation. If the cid is null, nothing is done and the current
    * transient conversation is resumed
    * 
    * 
    * @param cid The conversation id to restore
    * @return The conversation manager
    * @throws NonexistentConversationException If the non-transient conversation is not known
    * @throws BusyConversationException If the conversation is locked and not released while waiting 
    * @throws IllegalStateException if the conversation context is not active
    */
   
   public abstract ConversationManager2 setupConversation(String cid);
   
   /**
    * Destroys the current conversation if it's transient. Stores it for conversation 
    * propagation if it's non-transient
    * 
    * @return The conversation manager
    * @throws IllegalStateException if the conversation context is not active
    */
   public abstract ConversationManager2 teardownConversation();
   
   /**
    * Gets the current non-transient conversations
    * 
    * @return The conversations, mapped by id
    * @throws IllegalStateException if the conversation context is not active
    */
   public abstract Map<String, Conversation> getConversations();

   /**
    * Returns a new, session-unique conversation ID
    * 
    * @return The conversation id
    * @throws IllegalStateException if the conversation context is not active
    */   
   public abstract String generateConversationId();
 
}
