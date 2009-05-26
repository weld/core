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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.SessionScoped;

import org.jboss.webbeans.WebBean;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A ConversationTerminator implementation using Java SE scheduling
 *   
 * @author Nicklas Karlsson
 * @see org.jboss.webbeans.conversation.ConversationTerminator
 */
@SessionScoped
@WebBean
public class JavaSEConversationTerminator implements ConversationTerminator, Serializable
{
   private static final long serialVersionUID = 7258623232951724618L;

   private static LogProvider log = Logging.getLogProvider(JavaSEConversationTerminator.class);

   private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

   public Future<?> scheduleForTermination(Runnable terminationTask, long timeoutInMilliseconds)
   {
      log.trace("Recieved a termination task to be run in " + timeoutInMilliseconds + "ms");
      return executor.schedule(terminationTask, timeoutInMilliseconds, TimeUnit.MILLISECONDS);
   }

}
