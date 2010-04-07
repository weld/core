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

import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_SCOPE_BEAN_STORE_MISSING;
import static org.jboss.weld.servlet.BeanProvider.conversationManager;
import static org.jboss.weld.servlet.BeanProvider.httpSessionManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.conversation.ServletConversationManager;
import org.jboss.weld.exceptions.ForbiddenStateException;

/**
 * Implementation of the Weld lifecycle that can react to servlet events and
 * drives the Servlet based lifecycles of the Session, Conversation and Request
 * contexts
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 * @author David Allen
 */
public class ServletLifecycle
{
   private final ContextLifecycle lifecycle;

   private static class RequestBeanStoreCache
   {
      private static final String REQUEST_ATTRIBUTE_NAME = ServletLifecycle.class.getName() + ".requestBeanStore";

      public static void clear(HttpServletRequest request)
      {
         request.removeAttribute(REQUEST_ATTRIBUTE_NAME);
      }

      public static BeanStore get(HttpServletRequest request)
      {
         return (BeanStore) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
      }

      public static void set(HttpServletRequest request, BeanStore requestBeanStore)
      {
         request.setAttribute(REQUEST_ATTRIBUTE_NAME, requestBeanStore);
      }

      public static boolean isSet(HttpServletRequest request)
      {
         return get(request) != null;
      }
   }

   /**
    * Constructor
    * 
    * @param lifecycle The lifecycle to work against
    */
   public ServletLifecycle(ContextLifecycle lifecycle)
   {
      this.lifecycle = lifecycle;
   }

   /**
    * Begins a HTTP request. Sets the session into the session context
    * 
    * @param request The request
    */
   public void beginRequest(HttpServletRequest request)
   {
      if (!RequestBeanStoreCache.isSet(request))
      {
         BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
         RequestBeanStoreCache.set(request, requestBeanStore);
         lifecycle.beginRequest(request.getRequestURI(), requestBeanStore);
         restoreSessionContext(request);
         restoreConversationContext(request);
      }
   }

   /**
    * Restore the session from the underlying session object. Also allow the
    * session to be injected by the Session manager
    * 
    * @param session
    * @return the session bean store
    */
   protected BeanStore restoreSessionContext(HttpServletRequest request)
   {
      HttpPassThruSessionBeanStore sessionBeanStore = HttpPassThruOnDemandSessionBeanStore.of(request);
      HttpSession session = request.getSession(true);
      String sessionId = session == null ? "Inactive session" : session.getId();
      lifecycle.restoreSession(sessionId, sessionBeanStore);
      if (session != null)
      {
         sessionBeanStore.attachToSession(session);
         httpSessionManager(session.getServletContext()).setSession(session);
      }
      return sessionBeanStore;
   }

   private void restoreConversationContext(HttpServletRequest request)
   {
      // FIXME: HC "cid"
      conversationManager(request.getSession().getServletContext()).setupConversation(request.getParameter("cid"));
   }

   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public void beginSession(HttpSession session)
   {
      HttpPassThruSessionBeanStore beanStore = getSessionBeanStore();
      if (beanStore == null)
      {
         restoreSessionContext(session);
      }
      else
      {
         beanStore.attachToSession(session);
      }
   }

   private HttpPassThruSessionBeanStore getSessionBeanStore()
   {
      return (HttpPassThruSessionBeanStore) lifecycle.getSessionContext().getBeanStore();
   }

   protected BeanStore restoreSessionContext(HttpSession session)
   {
      String sessionId = session.getId();
      HttpPassThruSessionBeanStore beanStore = new HttpPassThruSessionBeanStore();
      beanStore.attachToSession(session);
      lifecycle.restoreSession(sessionId, beanStore);
      cacheSession(session);
      return beanStore;
   }

   private void cacheSession(HttpSession session)
   {
      httpSessionManager(session.getServletContext()).setSession(session);
   }

   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public void endRequest(HttpServletRequest request)
   {
      if (!RequestBeanStoreCache.isSet(request))
      {
         return;
      }
      teardownConversation();
      teardownSession(request);
      teardownRequest(request);
   }

   private void teardownRequest(HttpServletRequest request)
   {
      BeanStore beanStore = RequestBeanStoreCache.get(request);
      if (beanStore == null)
      {
         throw new ForbiddenStateException(REQUEST_SCOPE_BEAN_STORE_MISSING);
      }
      lifecycle.endRequest(request.getRequestURI(), beanStore);
      RequestBeanStoreCache.clear(request);
   }

   private void teardownConversation()
   {
      conversationManager(getServletContext()).teardownConversation();
   }

   private ServletContext getServletContext()
   {
      return getSessionBeanStore().getServletContext();
   }

   private void teardownSession(HttpServletRequest request)
   {
      HttpPassThruSessionBeanStore sessionBeanStore = getSessionBeanStore();
      if (isSessionBeanStoreInvalid(sessionBeanStore))
      {
         conversationManager(getServletContext()).teardownContext();
         lifecycle.endSession(request.getRequestedSessionId(), sessionBeanStore);
      }
      lifecycle.deactivateSessionContext();
   }

   private boolean isSessionBeanStoreInvalid(HttpPassThruSessionBeanStore sessionBeanStore)
   {
      return sessionBeanStore != null && sessionBeanStore.isInvalidated();
   }

   /**
    * Ends a session, setting up a mock request if necessary
    * 
    * @param session The HTTP session
    */
   public void endSession(HttpSession session)
   {
      if (lifecycle.isSessionContextActive())
      {
         activeSessionTermination(session);
      }
      else if (lifecycle.isRequestContextActive())
      {
         activeRequestTermination(session);
      }
      else
      {
         mockedSessionTermination(session);
      }
   }

   private void activeRequestTermination(HttpSession session)
   {
      String sessionId = session.getId();
      BeanStore store = restoreSessionContext(session);
      getServletConversationManager().destroyBackgroundConversations();
      lifecycle.endSession(sessionId, store);
   }

   private ServletConversationManager getServletConversationManager()
   {
      return (ServletConversationManager) conversationManager(getServletContext());
   }

   private void activeSessionTermination(HttpSession session)
   {
      String sessionId = session.getId();
      HttpPassThruSessionBeanStore beanStore = getSessionBeanStore();
      if (lifecycle.isRequestContextActive())
      {
         // Probably invalidated during request. This will be terminated
         // at the end of the request.
         beanStore.invalidate();
         getServletConversationManager().invalidateSession();
      }
      else
      {
         lifecycle.endSession(sessionId, beanStore);
      }
   }

   private void mockedSessionTermination(HttpSession session)
   {
      String sessionId = session.getId();
      BeanStore mockRequest = new ConcurrentHashMapBeanStore();
      lifecycle.beginRequest("endSession-" + sessionId, mockRequest);
      BeanStore sessionBeanStore = restoreSessionContext(session);
      lifecycle.endSession(session.getId(), sessionBeanStore);
      lifecycle.endRequest("endSession-" + sessionId, mockRequest);
   }

}
