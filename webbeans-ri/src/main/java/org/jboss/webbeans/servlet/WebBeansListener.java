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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * The Web Beans listener
 * 
 * Listens for context/session creation/destruction.
 * 
 * Delegates work to the ServletLifeCycle.
 * 
 * @author Nicklas Karlsson
 *
 */
public class WebBeansListener implements ServletContextListener, HttpSessionListener, ServletRequestListener
{

   /**
    * Called when the context is initialized (application started)
    * 
    * @param event The context event
    */
   public void contextInitialized(ServletContextEvent event) 
   {
      ServletLifecycle.instance().beginApplication(event.getServletContext());
   }

   /**
    * Called when the session is created
    * 
    * @param event The session event
    */
   public void sessionCreated(HttpSessionEvent event) 
   {
      ServletLifecycle.instance().beginSession(event.getSession());
   }

   /**
    * Called when the session is destroyed
    * 
    * @param event The session event
    */
   public void sessionDestroyed(HttpSessionEvent event) 
   {
      ServletLifecycle.instance().endSession(event.getSession());
   }

   /**
    * Called when the context is destroyed (application scoped)
    * 
    * @param event The context event
    */
   public void contextDestroyed(ServletContextEvent event) 
   {
      ServletLifecycle.instance().endApplication();
   }

   /**
    * Called when the request is destroyed
    * 
    * @param event The request event
    */
   public void requestDestroyed(ServletRequestEvent event)
   {
      if (event.getServletRequest() instanceof HttpServletRequest)
      {
         ServletLifecycle.instance().endRequest((HttpServletRequest) event.getServletRequest());
      }
      else
      {
         throw new IllegalStateException("Non HTTP-Servlet lifecycle not defined");
      }
   }

   /**
    * Called when the request is initialized
    * 
    * @param event The request event
    */
   public void requestInitialized(ServletRequestEvent event)
   {
      if (event.getServletRequest() instanceof HttpServletRequest)
      {
         ServletLifecycle.instance().beginRequest((HttpServletRequest) event.getServletRequest());
      }
      else
      {
         throw new IllegalStateException("Non HTTP-Servlet lifecycle not defined");
      }
   }

}
