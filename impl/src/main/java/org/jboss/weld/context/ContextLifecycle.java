/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
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
package org.jboss.weld.context;

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ContextMessage.APPLICATION_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.APPLICATION_STARTED;
import static org.jboss.weld.logging.messages.ContextMessage.CONVERSATION_RESTORED;
import static org.jboss.weld.logging.messages.ContextMessage.REQUEST_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.REQUEST_STARTED;
import static org.jboss.weld.logging.messages.ContextMessage.SESSION_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.SESSION_RESTORED;

import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.slf4j.cal10n.LocLogger;

/**
 * An implementation of the Weld lifecycle that supports restoring and
 * destroying all the built in contexts
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 * 
 */
public class ContextLifecycle implements Lifecycle, Service
{
   /*
    * Naming conventions:
    * 
    * "activating a context" = setting a non-null BeanStore and setting active to true
    * "deactivating a context" = setting a null BeanStore and setting active to false
    * "destroying a context" = activating the context, calling destroy(), deactivating the context
    * "restoring a context" = alias for activating the context
    * 
    */
   
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

   public void cleanup()
   {
      dependentContext.cleanup();
      requestContext.cleanup();
      conversationContext.cleanup();
      sessionContext.cleanup();
      singletonContext.cleanup();
      applicationContext.cleanup();
   }

   public void beginApplication(BeanStore applicationBeanStore)
   {
      log.trace(APPLICATION_STARTED, "");
      activateApplicationContext(applicationBeanStore);
      activateSingletonContext();
   }

   private void activateApplicationContext(BeanStore applicationBeanStore)
   {
      activateContext(applicationContext, applicationBeanStore);
   }

   private void activateContext(AbstractApplicationContext context, BeanStore beanStore)
   {
      if (beanStore == null)
      {
         throw new IllegalArgumentException("null bean store for " + context);
      }
      context.setBeanStore(beanStore);
      context.setActive(true);
   }

   private void activateSingletonContext()
   {
      activateContext(singletonContext, new ConcurrentHashMapBeanStore());
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_STARTED, id);
      activateDependentContext();
      activateRequestContext(requestBeanStore);
      activateConversationContext();
      activateSessionContext();
   }

   private void activateDependentContext()
   {
      dependentContext.setActive(true);
   }

   private void activateRequestContext(BeanStore requestBeanStore)
   {
      activateContext(requestContext, requestBeanStore);
   }

   private void activateContext(AbstractThreadLocalMapContext context, BeanStore beanStore)
   {
      if (beanStore == null)
      {
         throw new IllegalArgumentException("null bean store for " + context);
      }
      context.setBeanStore(beanStore);
      context.setActive(true);
   }

   private void activateConversationContext()
   {
      activateContext(conversationContext, new HashMapBeanStore());
   }

   private void activateSessionContext()
   {
//      activateContext(sessionContext, new HttpPassThruSessionBeanStore());
      sessionContext.setActive(true);
   }

   public void endApplication()
   {
      log.trace(APPLICATION_ENDED, "");
      destroyApplicationContext();
      destroySingletonContext();
   }

   private void destroyApplicationContext()
   {
      destroyContext(applicationContext);
   }

   private void destroyContext(AbstractApplicationContext context)
   {
      if (context.getBeanStore() == null)
      {
         return;
      }
      activateContext(context, context.getBeanStore());
      context.destroy();
      deactivateContext(context);
   }

   private void deactivateContext(AbstractApplicationContext context)
   {
      context.setBeanStore(null);
      context.setActive(false);
   }

   private void destroySingletonContext()
   {
      destroyContext(singletonContext);
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_ENDED, id);
      deactivateDependentContext();
      destroyRequestContext(requestBeanStore);
      deactivateConversationContext();
      deactivateSessionContext();
   }

   private void deactivateDependentContext()
   {
      dependentContext.setActive(false);
   }

   private void destroyRequestContext(BeanStore requestBeanStore)
   {
      destroyContext(requestContext, requestBeanStore);
   }

   private void deactivateConversationContext()
   {
      deactivateContext(conversationContext);
   }

   public void deactivateSessionContext()
   {
      deactivateContext(sessionContext);
   }

   public void endSession(String id, BeanStore sessionBeanStore)
   {
      log.trace(SESSION_ENDED, id);
      destroySessionContext(sessionBeanStore);
   }

   private void destroyConversationContext()
   {
      destroyContext(conversationContext, conversationContext.getBeanStore());
   }

   private void destroySessionContext(BeanStore sessionBeanStore)
   {
      destroyContext(sessionContext, sessionBeanStore);
   }

   private void destroyContext(AbstractThreadLocalMapContext context, BeanStore beanStore)
   {
      activateContext(context, beanStore);
      context.destroy();
      deactivateContext(context);
   }

   private void deactivateContext(AbstractThreadLocalMapContext context)
   {
      context.setBeanStore(null);
      context.setActive(false);
   }

   public void restoreSession(String id, BeanStore sessionBeanStore)
   {
      log.trace(SESSION_RESTORED, id);
      activateSessionContext(sessionBeanStore);
   }

   private void activateSessionContext(BeanStore sessionBeanStore)
   {
      activateContext(sessionContext, sessionBeanStore);
   }

   public void restoreConversation(String id, BeanStore conversationBeanStore)
   {
      log.trace(CONVERSATION_RESTORED, id);
      activateConversationContext(new HashMapBeanStore());
      conversationContext.loadTransientBeanStore(conversationBeanStore);
   }

   private void activateConversationContext(BeanStore conversationBeanStore)
   {
      activateContext(conversationContext, conversationBeanStore);
   }

   public void setupConversationContext()
   {
      activateConversationContext();
   }

   public void teardownConversationContext()
   {
      destroyConversationContext();
   }

   public boolean isSessionContextActive()
   {
      return sessionContext.isActive();
   }

   public boolean isRequestContextActive()
   {
      return requestContext.isActive();
   }

}