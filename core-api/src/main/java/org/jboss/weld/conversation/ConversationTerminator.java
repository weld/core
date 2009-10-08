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
package org.jboss.weld.conversation;

import java.util.concurrent.Future;

/**
 * A conversation terminator for scheduling inactivity timeout destructions
 * 
 * @author Nicklas Karlsson
 *
 */
public interface ConversationTerminator
{
   /**
    * Schedules a termination
    * 
    * @param terminationTask The termination task to run
    * @param timeoutInMilliseconds The timeout in milliseconds
    * 
    * @return A handle for manipulating the task later on
    */
   public abstract Future<?> scheduleForTermination(Runnable terminationTask, long timeoutInMilliseconds);
}
