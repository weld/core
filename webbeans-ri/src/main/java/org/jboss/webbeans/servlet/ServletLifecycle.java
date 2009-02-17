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
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.conversation.ConversationManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * Reacts to phases of the servlet life cycles
 * 
 * @author Pete Muir
 * @author Nicklas Karlsson
 */
public class ServletLifecycle
{
   
   private static final ServletLifecycle lifecycle = new ServletLifecycle();
   
   public static ServletLifecycle instance()
   {
      return lifecycle;
   }
   
   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);

   /**
    * Starts the application
    * 
    * Runs the bootstrapper for bean discover and initialization
    * 
    * @param context The servlet context
    */
   public void beginApplication(ServletContext servletContext)
   {
      log.trace("Application is starting up");
      new ServletInitialization(servletContext).boot();
   }

   /**
    * Ends the application
    */
   public void endApplication()
   {
      log.trace("Application is shutting down");
      ApplicationContext.INSTANCE.destroy();
      ApplicationContext.INSTANCE.setBeanMap(null);
   }

   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public void beginSession(HttpSession session)
   {
      log.trace("Starting session " + session.getId());
   }

   /**
    * Ends a session
    * 
    * @param session The HTTP session
    */
   public void endSession(HttpSession session)
   {
      log.trace("Ending session " + session.getId());
      SessionContext.INSTANCE.setBeanMap(new HttpSessionBeanMap(session));
      CurrentManager.rootManager().getInstanceByType(HttpSessionManager.class).setSession(session);
      ConversationManager conversationManager = CurrentManager.rootManager().getInstanceByType(ConversationManager.class);
      conversationManager.destroyAllConversations();
      SessionContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
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
      log.trace("Processing HTTP request " + request.getRequestURI() + " begins");
      SessionContext.INSTANCE.setBeanMap(new HttpSessionBeanMap(request.getSession()));
      DependentContext.INSTANCE.setActive(true);
   }

   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public void endRequest(HttpServletRequest request)
   {
      log.trace("Processing HTTP request " + request.getRequestURI() + " ends");
      DependentContext.INSTANCE.setActive(false);
      RequestContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
   }

}
