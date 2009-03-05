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

package org.jboss.webbeans.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * Implementation of the Web Beans lifecycle that can react to servlet events
 * 
 * This implementation boots the Web Beans container.
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class ServletLifecycle extends AbstractLifecycle
{

   public static final String REQUEST_ATTRIBUTE_NAME = ServletLifecycle.class.getName() + ".requestBeanStore";

   static
   {
      AbstractLifecycle.setInstance(new ServletLifecycle());
   }

   public static ServletLifecycle instance()
   {
      return (ServletLifecycle) AbstractLifecycle.instance();
   }

   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);

   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public void beginSession(HttpSession session)
   {
      super.beginSession(session.getId(), null);
   }

   /**
    * Ends a session
    * 
    * @param session The HTTP session
    */
   public void endSession(HttpSession session)
   {
      BeanStore mockRequest = new ConcurrentHashMapBeanStore();
      super.beginRequest("endSession-" + session.getId(), mockRequest);
      super.endSession(session.getId(), restoreSessionContext(session));
      super.endRequest("endSession-" + session.getId(), mockRequest);
   }

   /**
    * Restore the session from the underlying session object. Also allow the
    * session to be injected by the Session manager
    * 
    * @param session
    * @return the session bean store
    */
   protected BeanStore restoreSessionContext(HttpSession session)
   {
      BeanStore sessionBeanStore = new HttpSessionBeanStore(session);
      SessionContext.INSTANCE.setBeanStore(sessionBeanStore);
      CurrentManager.rootManager().getInstanceByType(HttpSessionManager.class).setSession(session);
      return sessionBeanStore;
   }

   /**
    * Begins a HTTP request
    * 
    * Sets the session into the session context
    * 
    * @param request The request
    */
   public void beginRequest(HttpServletRequest request)
   {
      BeanStore beanStore = new ConcurrentHashMapBeanStore();
      request.setAttribute(REQUEST_ATTRIBUTE_NAME, beanStore);
      super.beginRequest(request.getRequestURI(), beanStore);
      restoreSessionContext(request.getSession());
   }

   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public void endRequest(HttpServletRequest request)
   {
      BeanStore beanStore = (BeanStore) request.getAttribute(REQUEST_ATTRIBUTE_NAME);
      request.removeAttribute(REQUEST_ATTRIBUTE_NAME);
      super.endRequest(request.getRequestURI(), beanStore);
      SessionContext.INSTANCE.setBeanStore(null);
   }

}
