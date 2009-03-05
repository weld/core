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

import java.util.Set;

import javax.context.Conversation;

/**
 * A conversation manager responsible for starting, resuming and ending conversations
 * 
 * @author Nicklas Karlsson
 * @see org.jboss.webbeans.conversation.ConversationManager
 */
public interface ConversationManager
{
   /**
    * Begins or restores a conversation
    * 
    * @param cid The incoming conversation ID. Can be null in cases of transient conversations
    */
   public abstract void beginOrRestoreConversation(String cid);
   
   /**
    * Cleans up the current conversation, destroying transient conversation and handling 
    * long-running conversations
    */
   public abstract void cleanupConversation();
   
   /**
    * Destroys all long-running conversations
    */
   public abstract void destroyAllConversations();
   
   /**
    * Gets the currently managed long-running conversations
    * 
    * @return the conversations
    */
   public abstract Set<Conversation> getLongRunningConversations();
   
}
