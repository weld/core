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

import static org.jboss.weld.jsf.JsfHelper.getServletContext;
import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ContextMessage.APPLICATION_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.APPLICATION_STARTED;
import static org.jboss.weld.logging.messages.ContextMessage.REQUEST_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.REQUEST_STARTED;
import static org.jboss.weld.logging.messages.ContextMessage.SESSION_ENDED;
import static org.jboss.weld.logging.messages.ContextMessage.SESSION_RESTORED;
import static org.jboss.weld.servlet.BeanProvider.conversationManager;

import javax.faces.context.FacesContext;

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
      setupContext(sessionContext, sessionBeanStore);
   }

   public void endSession(String id, BeanStore sessionBeanStore)
   {
      log.trace(SESSION_ENDED, id);
      teardownContext(sessionContext);
      conversationManager(getServletContext(FacesContext.getCurrentInstance())).teardownContext();
   }

   public void beginRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_STARTED, id);
      dependentContext.setActive(true);
      setupContext(requestContext, requestBeanStore);
      setupConversationContext();
   }

   public void endRequest(String id, BeanStore requestBeanStore)
   {
      log.trace(REQUEST_ENDED, id);
      requestContext.setBeanStore(requestBeanStore);
      dependentContext.setActive(false);
      teardownContext(requestContext);
      conversationContext.setBeanStore(null);
      conversationContext.setActive(false);
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
      setupContext(applicationContext, applicationBeanStore);
      setupContext(singletonContext, new ConcurrentHashMapBeanStore());
   }

   public void endApplication()
   {
      log.trace(APPLICATION_ENDED, "");
      teardownContext(applicationContext);
      teardownContext(singletonContext);
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

   public void setupConversationContext()
   {
      setupContext(conversationContext, new HashMapBeanStore());
   }

   public void teardownConversationContext()
   {
      teardownContext(conversationContext);
   }

   private void setupContext(AbstractThreadLocalMapContext context, BeanStore beanStore)
   {
      context.setBeanStore(beanStore);
      context.setActive(true);
   }

   private void setupContext(AbstractApplicationContext context, BeanStore beanStore)
   {
      context.setBeanStore(beanStore);
      context.setActive(true);
   }
   
   private void teardownContext(AbstractThreadLocalMapContext context)
   {
      context.setActive(true);
      context.destroy();
      context.setBeanStore(null);
      context.setActive(false);
   }
   
   private void teardownContext(AbstractApplicationContext context)
   {
      context.setActive(true);
      context.destroy();
      context.setBeanStore(null);
      context.setActive(false);
   }   

}
