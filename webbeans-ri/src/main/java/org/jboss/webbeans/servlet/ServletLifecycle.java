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

import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.beanmap.SessionBeanMap;
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
   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);
   
   /**
    * Starts the application
    * 
    * Runs the bootstrapper for bean discover and initialization
    * 
    * @param context The servlet context
    */
   public static void beginApplication(ServletContext servletContext)
   {
      new ServletBootstrap(servletContext).boot();
   }
   
   /**
    * Ends the application
    */
   public static void endApplication() 
   {
      ApplicationContext.INSTANCE.destroy();
      ApplicationContext.INSTANCE.setBeanMap(null);
   }
   
   /**
    * Begins a session
    * 
    * @param session The HTTP session
    */
   public static void beginSession(HttpSession session)
   {
   }
   
   /**
    * Ends a session
    * 
    * @param session The HTTP session
    */
   public static void endSession(HttpSession session) 
   {
      SessionContext.INSTANCE.setBeanMap(new SessionBeanMap(session));
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
   public static void beginRequest(HttpServletRequest request) 
   {
      SessionContext.INSTANCE.setBeanMap(new SessionBeanMap(request.getSession()));
      DependentContext.INSTANCE.setActive(true);
   }
   
   /**
    * Ends a HTTP request
    * 
    * @param request The request
    */
   public static void endRequest(HttpServletRequest request) 
   {
      DependentContext.INSTANCE.setActive(false);
      RequestContext.INSTANCE.destroy();
      SessionContext.INSTANCE.setBeanMap(null);
   }
   
}
