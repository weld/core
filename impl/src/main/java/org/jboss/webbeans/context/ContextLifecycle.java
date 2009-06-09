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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bootstrap.api.Lifecycle;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ConversationBeanStore;

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
   
   @Any private Instance<ConversationManager> conversationManager;

   public void restoreSession(String id, BeanStore sessionBeanStore)
   {
      log.trace("Restoring session " + id);
      SessionContext.instance().setBeanStore(sessionBeanStore);
      SessionContext.instance().setActive(true);
   }

   public void endSession(String id, BeanStore sessionBeanStore)
   {
      log.trace("Ending session " + id);
      SessionContext.instance().setActive(true);
      ConversationManager conversationManager = CurrentManager.rootManager().getInstanceByType(ConversationManager.class);
      conversationManager.destroyAllConversations();
      SessionContext.instance().destroy();
      SessionContext.instance().setBeanStore(null);
      SessionContext.instance().setActive(false);
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Starting request " + id);
      RequestContext.instance().setBeanStore(requestBeanStore);
      RequestContext.instance().setActive(true);
      DependentContext.instance().setActive(true);
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace("Ending request " + id);
      RequestContext.instance().setBeanStore(requestBeanStore);
      DependentContext.instance().setActive(false);
      RequestContext.instance().destroy();
      RequestContext.instance().setActive(false);
   }

   protected void restoreConversation(String id, BeanStore conversationBeanStore)
   {
      log.trace("Starting conversation " + id);
      ConversationContext.instance().setBeanStore(conversationBeanStore);
      ConversationContext.instance().setActive(true);
   }

   protected void destroyConversation(String id, ConversationBeanStore conversationBeanStore)
   {
      log.trace("Ending conversation " + id);
      ConversationContext destructionContext = new ConversationContext();
      destructionContext.setBeanStore(conversationBeanStore);
      destructionContext.destroy();
   }

}
