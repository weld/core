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
package javax.enterprise.context;

/**
 * Provides conversation management operations
 * 
 * @author Pete Muir
 * 
 */
public interface Conversation
{

   /**
    * Mark a transient conversation long running. The container will generate an
    * id
    * 
    * @throws IllegalStateException if the current conversation is marked long
    *            running
    */
   public void begin();

   /**
    * Mark a transient conversation long running.
    * 
    * @param id the id of the conversation
    * @throws IllegalStateException if the current conversation is marked long
    *            running
    * @throws IllegalArgumentException if a long running conversation with id
    *            already exists
    */
   public void begin(String id);

   /**
    * Mark a long running conversation transient
    * 
    * @throws IllegalStateException if the current conversation is marked
    *            transient
    */
   public void end();

   /**
    * Determine if a conversation is long running or transient
    * 
    * @return true if the conversation is long running
    */
   public boolean isLongRunning();

   /**
    * Get the id associated with the current long running conversation
    * 
    * @return the id of the current long running conversation
    */
   public String getId();

   /**
    * Get the timeout for the current long running conversation.
    * 
    * The conversation will destroy the conversation if it has not been accessed
    * within this time period.
    * 
    * @return the current timeout in milliseconds
    */
   public long getTimeout();

   /**
    * Set the timeout for the current long running conversation
    * 
    * @param milliseconds the new timeout in milliseconds
    */
   public void setTimeout(long milliseconds);

   /**
    * @return true if the conversation is marked transient, or false if it is
    *         marked long-running.
    */
   public boolean isTransient();
}