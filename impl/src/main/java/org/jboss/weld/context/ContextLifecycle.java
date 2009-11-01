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
package org.jboss.weld.context;

import static org.jboss.weld.messages.ContextMessage.APPLICATION_ENDED;
import static org.jboss.weld.messages.ContextMessage.APPLICATION_STARTED;
import static org.jboss.weld.messages.ContextMessage.REQUEST_ENDED;
import static org.jboss.weld.messages.ContextMessage.REQUEST_STARTED;
import static org.jboss.weld.messages.ContextMessage.SESSION_ENDED;
import static org.jboss.weld.messages.ContextMessage.SESSION_RESTORED;
import static org.jboss.weld.util.log.Category.CONTEXT;
import static org.jboss.weld.util.log.LoggerFactory.loggerFactory;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.slf4j.cal10n.LocLogger;

/**
 * An implementation of the Weld lifecycle that supports restoring
 * and destroying all the built in contexts
 * 
 * @author Pete Muir
 * 
 */
public class ContextLifecycle implements Lifecycle, Service
{
   
   private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

   private final AbstractApplicationContext applicationContext;
   private final AbstractApplicationContext singletonContext;
   private final SessionContext sessionContext;
   private final ConversationContext conversationContext;
   private final RequestContext requestContext;
   private final DependentContext dependentContext;
   
   public ContextLifecycle(AbstractApplicationContext applicationContext, AbstractApplicationContext singletonContext, SessionContext sessionContext, ConversationContext conversationContext, RequestContext requestContext, DependentContext dependentContext)
   {
      this.applicationContext = applicationContext;
      this.singletonContext = singletonContext;
      this.sessionContext = sessionContext;
      this.conversationContext = conversationContext;
      this.requestContext = requestContext;
      this.dependentContext = dependentContext;
   }

   public void restoreSession(String id, BeanStore sessionBeanStore)
   {
      log.trace(SESSION_RESTORED, id);
      sessionContext.setBeanStore(sessionBeanStore);
      sessionContext.setActive(true);
   }

   public void endSession(String id, BeanStore sessionBeanStore)
   {
      log.trace(SESSION_ENDED, id);
      sessionContext.setActive(true);
      sessionContext.destroy();
      sessionContext.setBeanStore(null);
      sessionContext.setActive(false);
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_STARTED, id);
      requestContext.setBeanStore(requestBeanStore);
      requestContext.setActive(true);
      dependentContext.setActive(true);
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_ENDED, id);
      requestContext.setBeanStore(requestBeanStore);
      dependentContext.setActive(false);
      requestContext.destroy();
      requestContext.setActive(false);
      requestContext.setBeanStore(null);
   }
   
   public boolean isRequestActive()
   {
      return singletonContext.isActive() && applicationContext.isActive() && requestContext.isActive() && dependentContext.isActive();
   }
   
   public boolean isApplicationActive()
   {
      return singletonContext.isActive() && applicationContext.isActive() && dependentContext.isActive();
   }
   
   public boolean isConversationActive()
   {
      return singletonContext.isActive() && applicationContext.isActive() && sessionContext.isActive() && conversationContext.isActive() && dependentContext.isActive();
   }
   
   public boolean isSessionActive() 
   {
      return singletonContext.isActive() && applicationContext.isActive() && sessionContext.isActive() && dependentContext.isActive();
   }
   

   public void beginApplication(BeanStore applicationBeanStore)
   {
      log.trace(APPLICATION_STARTED, "");
      applicationContext.setBeanStore(applicationBeanStore);
      applicationContext.setActive(true);
      singletonContext.setBeanStore(new ConcurrentHashMapBeanStore());
      singletonContext.setActive(true);
   }
   
   public void endApplication()
   {
      log.trace(APPLICATION_ENDED, "");
      applicationContext.destroy();
      applicationContext.setActive(false);
      applicationContext.setBeanStore(null);
      singletonContext.destroy();
      singletonContext.setActive(false);
      singletonContext.setBeanStore(null);
      Container.instance().cleanup();
   }
   
   public void cleanup() 
   {
      dependentContext.cleanup();
      requestContext.cleanup();
      conversationContext.cleanup();
      sessionContext.cleanup();
      singletonContext.cleanup();
      applicationContext.cleanup();
   }
   
   public AbstractApplicationContext getApplicationContext()
   {
      return applicationContext;
   }
   
   public AbstractApplicationContext getSingletonContext()
   {
      return singletonContext;
   }
   
   public SessionContext getSessionContext()
   {
      return sessionContext;
   }
   
   public ConversationContext getConversationContext()
   {
      return conversationContext;
   }
   
   public RequestContext getRequestContext()
   {
      return requestContext;
   }
   
   public DependentContext getDependentContext()
   {
      return dependentContext;
   }

}
