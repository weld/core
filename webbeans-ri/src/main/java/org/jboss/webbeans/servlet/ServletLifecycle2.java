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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.beanstore.SimpleBeanStore;

/**
 * Implementation of the Web Beans lifecycle that can react to servlet events.
 * 
 * This implementation does not boot the Web Beans container.
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class ServletLifecycle2 extends AbstractLifecycle
{
   
   public static final String REQUEST_ATTRIBUTE_NAME = ServletLifecycle2.class.getName() + ".requestBeanStore";
   
   public static ServletLifecycle2 instance()
   {
      return (ServletLifecycle2) AbstractLifecycle.instance();
   }
   
   static
   {
      AbstractLifecycle.setInstance(new ServletLifecycle2());
   }

   /**
    * Starts the application
    * 
    * Runs the bootstrapper for bean discover and initialization
    * 
    * @param context The servlet context
    */
   public void beginApplication(ServletContext servletContext)
   {
      super.beginApplication(servletContext.getServletContextName(), new ApplicationBeanStore(servletContext));
   }

   /**
    * Ends the application
    */
   public void endApplication(ServletContext servletContext)
   {
      super.endApplication(servletContext.getServletContextName(), new ApplicationBeanStore(servletContext));
   }

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
      super.endSession(session.getId(), restoreSessionContext(session));
   }
   
   /**
    * Restore the session from the underlying session object. Also allow the 
    * session to be injected by the Session manager
    * 
    * @param session
    * @return
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
      restoreSessionContext(request.getSession());
      BeanStore beanStore = new SimpleBeanStore();
      request.setAttribute(REQUEST_ATTRIBUTE_NAME, beanStore);
      super.beginRequest(request.getRequestURI(), beanStore);
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
