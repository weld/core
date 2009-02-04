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

import javax.annotation.Named;
import javax.context.Conversation;
import javax.context.RequestScoped;

/**
 * The current conversation implementation
 * 
 * @author Nicklas Karlsson
 * @see javax.context.Conversation 
 */
@RequestScoped
@Named("conversation")
public class ConversationImpl implements Conversation
{
   // The conversation ID
   private String cid;
   // Is the conversation long-running?
   private boolean longRunning;
   // The inactivity timeout in milliseconds
   private long timeoutInMilliseconds;

   /**
    * Creates a new conversation 
    * 
    * @param cid The conversation ID
    * @param timeoutInMilliseconds The inactivity timeout in milliseconds
    */
   protected ConversationImpl(String cid, long timeoutInMilliseconds)
   {
      this.timeoutInMilliseconds = timeoutInMilliseconds;
      this.cid = cid;
   }

   /**
    * Factory method
    * 
    * @param cid The conversation ID
    * @param timeoutInMilliseconds The inactivity timeout in milliseconds
    * @return A new conversation
    */
   public static ConversationImpl of(String cid, long timeoutInMilliseconds)
   {
      return new ConversationImpl(cid, timeoutInMilliseconds);
   }

   public void begin()
   {
      longRunning = true;
   }

   public void begin(String id)
   {
      longRunning = true;
      cid = id;
   }

   public void end()
   {
      longRunning = false;
   }

   public String getId()
   {
      return cid;
   }

   public long getTimeout()
   {
      return timeoutInMilliseconds;
   }

   public boolean isLongRunning()
   {
      return longRunning;
   }

   public void setTimeout(long timeout)
   {
      this.timeoutInMilliseconds = timeout;
   }

   /**
    * Assumes the identity of another conversation
    * 
    * @param cid The new conversation ID
    * @param longRunning The new long-running status
    * @param timeout The new inactivity timeout in milliseconds 
    */
   public void become(String cid, boolean longRunning, long timeoutInMilliseconds)
   {
      this.cid = cid;
      this.longRunning = longRunning;
      this.timeoutInMilliseconds = timeoutInMilliseconds;
   }

   @Override
   public String toString()
   {
      return "ID: " + cid + ", long-running: " + longRunning + ", timeout: " + timeoutInMilliseconds;
   }

   public void setLongRunning(boolean longRunning)
   {
      this.longRunning = longRunning;
   }
}
