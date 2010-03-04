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

import static org.jboss.weld.jsf.JsfHelper.getServletContext;
import static org.jboss.weld.logging.messages.ServletMessage.REQUEST_SCOPE_BEAN_STORE_MISSING;
import static org.jboss.weld.servlet.BeanProvider.conversationManager;
import static org.jboss.weld.servlet.BeanProvider.httpSessionManager;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.conversation.ServletConversationManager;
import org.jboss.weld.exceptions.ForbiddenStateException;

/**
 * Implementation of the Weld lifecycle that can react to servlet events and
 * drives the Session, Conversation and Request (for Servlet requests) lifecycle
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 * @author David Allen
 */
public class ServletLifecycle
{

   private final ContextLifecycle lifecycle;

   public static final String REQUEST_ATTRIBUTE_NAME = ServletLifecycle.class.getName() + ".requestBeanStore";

   /**
    * 
    */
   public ServletLifecycle(ContextLifecycle lifecycle)
   {
      this.lifecycle = lifecycle;
   }

   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public void beginSession(HttpSession session)
   {
      HttpPassThruSessionBeanStore beanStore = (HttpPassThruSessionBeanStore) lifecycle.getSessionContext().getBeanStore();
      if (beanStore == null)
      {
         restoreSessionContext(session);
      }
      else
      {
         beanStore.attachToSession(session);
      }
   }

   /**
    * Ends a session, setting up a mock request if necessary
    * 
    * @param session The HTTP session
    */
   public void endSession(HttpSession session)
   {
      ServletConversationManager conversationManager = (ServletConversationManager) conversationManager(session.getServletContext());
      if (lifecycle.getSessionContext().isActive())
      {
         HttpPassThruSessionBeanStore beanStore = (HttpPassThruSessionBeanStore) lifecycle.getSessionContext().getBeanStore();
         if (lifecycle.getRequestContext().isActive())
         {
            // Probably invalidated during request. This will be terminated
            // at the end of the request.
            beanStore.invalidate();
            conversationManager.invalidateSession();
         }
         else
         {
            lifecycle.endSession(session.getId(), beanStore);
         }
      }
      else if (lifecycle.getRequestContext().isActive())
      {
         BeanStore store = restoreSessionContext(session);
         conversationManager.destroyAllConversations();
         lifecycle.endSession(session.getId(), store);
      }
      else
      {
         BeanStore mockRequest = new ConcurrentHashMapBeanStore();

         lifecycle.beginRequest("endSession-" + session.getId(), mockRequest);
         BeanStore sessionBeanStore = restoreSessionContext(session);
         lifecycle.endSession(session.getId(), sessionBeanStore);
         lifecycle.endRequest("endSession-" + session.getId(), mockRequest);
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
      HttpPassThruSessionBeanStore sessionBeanStore = new HttpPassThruOnDemandSessionBeanStore(request);
      HttpSession session = request.getSession(true);
      lifecycle.restoreSession(session == null ? "Inactive session" : session.getId(), sessionBeanStore);
      if (session != null)
      {
         sessionBeanStore.attachToSession(session);
         httpSessionManager(session.getServletContext()).setSession(session);
      }
      return sessionBeanStore;
   }

   protected BeanStore restoreSessionContext(HttpSession session)
   {
      HttpPassThruSessionBeanStore beanStore = new HttpPassThruSessionBeanStore();
      beanStore.attachToSession(session);
      lifecycle.restoreSession(session.getId(), beanStore);
      httpSessionManager(session.getServletContext()).setSession(session);
      return beanStore;
   }

   /**
    * Begins a HTTP request Sets the session into the session context
    * 
    * @param request The request
    */
   public void beginRequest(HttpServletRequest request)
   {
      if (request.getAttribute(REQUEST_ATTRIBUTE_NAME) == null)
      {
         BeanStore beanStore = new ConcurrentHashMapBeanStore();
         request.setAttribute(REQUEST_ATTRIBUTE_NAME, beanStore);
         lifecycle.beginRequest(request.getRequestURI(), beanStore);
         restoreSessionContext(request);
      }
   }

   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public void endRequest(HttpServletRequest request)
   {
      if (request.getAttribute(REQUEST_ATTRIBUTE_NAME) != null)
      {
         HttpPassThruSessionBeanStore sessionBeanStore = (HttpPassThruSessionBeanStore) lifecycle.getSessionContext().getBeanStore();
         if ((sessionBeanStore != null) && (sessionBeanStore.isInvalidated()))
         {
            conversationManager(request.getSession().getServletContext()).teardownContext();
            lifecycle.endSession(request.getRequestedSessionId(), sessionBeanStore);
         }
         lifecycle.getSessionContext().setActive(false);
         lifecycle.getSessionContext().setBeanStore(null);
         BeanStore beanStore = (BeanStore) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
         if (beanStore == null)
         {
            throw new ForbiddenStateException(REQUEST_SCOPE_BEAN_STORE_MISSING);
         }
         lifecycle.endRequest(request.getRequestURI(), beanStore);
         request.removeAttribute(REQUEST_ATTRIBUTE_NAME);
      }
   }

}
