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
package org.jboss.weld.servlet;

import static org.jboss.weld.logging.Category.SERVLET;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ServletMessage.ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_DESTROYED;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_INITIALIZED;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Instance;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.Container;
import org.jboss.weld.context.http.HttpConversationContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.servlet.api.helpers.AbstractServletListener;
import org.slf4j.cal10n.LocLogger;

/**
 * The Weld listener
 * 
 * Listens for context/session creation/destruction.
 * 
 * Delegates work to the ServletLifeCycle.
 * 
 * @author Nicklas Karlsson
 * 
 */
public class WeldListener extends AbstractServletListener
{

   private static final LocLogger log = loggerFactory().getLogger(SERVLET);

   @Override
   public void sessionDestroyed(HttpSessionEvent event)
   {
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         HttpSession session = event.getSession();
         Instance<Context> instance = instance();
         HttpSessionContext sessionContext = instance.select(HttpSessionContext.class).get();
         HttpConversationContext conversationContext = instance.select(HttpConversationContext.class).get();

         // Mark the session context and conversation contexts to destroy
         // instances when appropriate
         sessionContext.destroy(session);
      }
   }

   @Override
   public void requestDestroyed(ServletRequestEvent event)
   {
      log.trace(REQUEST_DESTROYED, event.getServletRequest());
      // JBoss AS will still start the deployment even if WB fails
      if (Container.available())
      {
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
            Instance<Context> instance = instance();

            HttpRequestContext requestContext = instance.select(HttpRequestContext.class).get();
            HttpSessionContext sessionContext = instance.select(HttpSessionContext.class).get();
            HttpConversationContext conversationContext = instance.select(HttpConversationContext.class).get();

            requestContext.invalidate();
            requestContext.deactivate();
            sessionContext.deactivate();
            /*
             * The conversation context is invalidated and deactivated in the
             * WeldPhaseListener
             */

            requestContext.dissociate(request);
            sessionContext.dissociate(request);
            conversationContext.dissociate(request);
         }
         else
         {
            throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
         }
      }
   }

   @Override
   public void requestInitialized(ServletRequestEvent event)
   {
      log.trace(REQUEST_INITIALIZED, event.getServletRequest());
      // JBoss AS will still start the deployment even if Weld fails to start
      if (Container.available())
      {
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            HttpServletRequest request = (HttpServletRequest) event.getServletRequest();
            Instance<Context> instance = instance();

            HttpRequestContext requestContext = instance.select(HttpRequestContext.class).get();
            HttpSessionContext sessionContext = instance.select(HttpSessionContext.class).get();
            HttpConversationContext conversationContext = instance.select(HttpConversationContext.class).get();

            requestContext.associate(request);
            sessionContext.associate(request);
            conversationContext.associate(request);
            /*
             * The conversation context is activated in the WeldPhaseListener
             */

            requestContext.activate();
            sessionContext.activate();
         }
         else
         {
            throw new IllegalStateException(ONLY_HTTP_SERVLET_LIFECYCLE_DEFINED);
         }

      }
   }
   
   private static Instance<Context> instance()
   {
      return Container.instance().deploymentManager().instance().select(Context.class);
   }

}
