/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.jetty7;

import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.weld.environment.jetty.JettyWeldInjector;
import org.jboss.weld.manager.api.WeldManager;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
 * @author <a href="mailto:dan.j.allen@gmail.com">Dan Allen</a>
*/
public class WeldServletHandler extends ServletHandler
{
   private ServletContext sco;
   private JettyWeldInjector injector;

   public WeldServletHandler(ServletHandler existingHandler, ServletContext servletContext)
   {
      sco = servletContext;
      setFilters(existingHandler.getFilters());
      setFilterMappings(existingHandler.getFilterMappings());
      setServlets(existingHandler.getServlets());
      setServletMappings(existingHandler.getServletMappings());
   }

   // Method was removed in Jetty 8, so don't mark as override
   //@Override
   public Servlet customizeServlet(Servlet servlet) throws Exception
   {
      inject(servlet);
      return servlet;
   }

   // Method was removed in Jetty 8, so don't mark as override
   //@Override
   public Filter customizeFilter(Filter filter) throws Exception
   {
      inject(filter);
      return filter;
   }

   protected void inject(Object injectable)
   {
      if (injector == null)
      {
         initializeInjector();
      }
      
      if (injector == null)
      {
         Log.warn("Can't find WeldManager in the servlet context; injection is not available for Servlet component: " + injectable);
      }
      else
      {
         injector.inject(injectable);
      }
   }

   public static void process(WebAppContext wac)
   {
      WeldServletHandler wHanlder = new WeldServletHandler(wac.getServletHandler(), wac.getServletContext());
      wac.setServletHandler(wHanlder);
      wac.getSecurityHandler().setHandler(wHanlder);
      
      // notify the Weld listener of the context initialized event earlier than other listeners
      // so that injection can be supported into other listeners
      boolean initialized = false;
      for (EventListener l : wac.getEventListeners())
      {
         if (l instanceof org.jboss.weld.environment.servlet.Listener)
         {
            ServletContext ctx = wac.getServletContext();
            ((org.jboss.weld.environment.servlet.Listener) l).contextInitialized(new ServletContextEvent(ctx));
            initialized = true;
            break;
         }
      }
      
      if (initialized)
      {
         for (EventListener l : wac.getEventListeners())
         {
            if (!(l instanceof Listener))
            {
               wHanlder.inject(l);
            }
         }
      }
   }
   
   protected void initializeInjector()
   {
      WeldManager manager = (WeldManager) sco.getAttribute(org.jboss.weld.environment.servlet.Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
      if (manager != null)
      {
         injector = new JettyWeldInjector(manager);
      }
   }
}
