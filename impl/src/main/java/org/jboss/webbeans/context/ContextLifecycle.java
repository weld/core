/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.webbeans.context;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bootstrap.api.Lifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * An implementation of the Web Beans lifecycle that supports restoring
 * and destroying all the built in contexts
 * 
 * @author Pete Muir
 * 
 */
public class ContextLifecycle implements Lifecycle
{

   private static LogProvider log = Logging.getLogProvider(ContextLifecycle.class);

   public void restoreSession(String id, BeanStore sessionBeanStore)
   {
      log.trace("Restoring session " + id);
      SessionContext sessionContext = CurrentManager.rootManager().getServices().get(SessionContext.class);
      sessionContext.setBeanStore(sessionBeanStore);
      sessionContext.setActive(true);
   }

   public void endSession(String id, BeanStore sessionBeanStore, ConversationManager conversationManager)
   {
      log.trace("Ending session " + id);
      SessionContext sessionContext = CurrentManager.rootManager().getServices().get(SessionContext.class);
      sessionContext.setActive(true);
      conversationManager.destroyAllConversations();
      sessionContext.destroy();
      sessionContext.setBeanStore(null);
      sessionContext.setActive(false);
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Starting request " + id);
      RequestContext requestContext = CurrentManager.rootManager().getServices().get(RequestContext.class);
      DependentContext dependentContext = CurrentManager.rootManager().getServices().get(DependentContext.class);
      requestContext.setBeanStore(requestBeanStore);
      requestContext.setActive(true);
      dependentContext.setActive(true);
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Ending request " + id);
      RequestContext requestContext = CurrentManager.rootManager().getServices().get(RequestContext.class);
      DependentContext dependentContext = CurrentManager.rootManager().getServices().get(DependentContext.class);
      requestContext.setBeanStore(requestBeanStore);
      dependentContext.setActive(false);
      requestContext.destroy();
      requestContext.setActive(false);
   }

}
