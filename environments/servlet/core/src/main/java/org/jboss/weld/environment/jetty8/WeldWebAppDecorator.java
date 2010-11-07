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
package org.jboss.weld.environment.jetty8;

import java.util.EventListener;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import org.eclipse.jetty.plus.webapp.WebAppDecorator;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.jboss.weld.environment.jetty.JettyWeldInjector;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.api.WeldManager;

/**
 * A Jetty decorator implementation that performs CDI injections and cleanup for
 * Servlet, Filter and Listener components.
 * 
 * <p>
 * This class wraps the post-instantiation and destruction of Servlet, Filter
 * and Listener components and performs CDI injections and release,
 * respectively, using the Jetty Weld injector.
 * </p>
 * 
 * <p>
 * Releasing of the creational context for Listener components is performed
 * using a Jetty Configuration to ensure that it happens after all Listener
 * components have been notified of the ServletContext destroyed event. This
 * workaround is necessary since Jetty does not yet notify the decorator of a
 * listener being destroyed.
 * </p>
 * 
 * @author <a href="mailto:dan.j.allen@gmail.com">Dan Allen</a>
 */
public class WeldWebAppDecorator extends WebAppDecorator implements Configuration
{
   private ServletContext ctx;
   private JettyWeldInjector injector;

   public WeldWebAppDecorator(WebAppContext context)
   {
      super(context);
      ctx = context.getServletContext();
   }
   
   public static void register(WebAppContext wac)
   {
      WeldWebAppDecorator d = new WeldWebAppDecorator(wac);
      wac.setDecorator(d);
      appendConfiguration(wac, d);
      // check if event listeners were already instantiated (happens in Jetty < 8.0.0.M1)
      if (wac.getEventListeners() != null)
      {
         for (EventListener l : wac.getEventListeners())
         {
            // preemptively invoke the Weld Listener
            if (l instanceof Listener)
            {
               d.initializeWeld(l);
            }
            else
            {
               d.inject(l);
            }
         }
      }
   }
   
   @Override
   public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException
   {
      return inject(super.decorateFilterInstance(filter));
   }

   @Override
   public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException
   {
      // preemptively invoke the Weld Listener
      if (listener instanceof Listener)
      {
         return initializeWeld(listener);
      }
      else
      {
         return inject(super.decorateListenerInstance(listener));
      }
   }

   @Override
   public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException
   {
      return inject(super.decorateServletInstance(servlet));
   }
   
   @Override
   public void destroyFilterInstance(Filter f)
   {
      super.destroyFilterInstance(f);
      release(f);
   }

   @Override
   public void destroyServletInstance(Servlet s)
   {
      super.destroyServletInstance(s);
      release(s);
   }

   /**
    * NOTE not yet invoked as of Jetty 8.0.0.M1
    */
   @Override
   public void destroyListenerInstance(EventListener l)
   {
      super.destroyListenerInstance(l);
      release(l);
   }

   
   public void deconfigure(WebAppContext context) throws Exception
   {
      // releases injections on Listener components
      if (injector != null)
      {
         injector.releaseAll();
      }
   }

   public void preConfigure(WebAppContext context) throws Exception {}

   public void configure(WebAppContext context) throws Exception {}

   public void postConfigure(WebAppContext context) throws Exception {}

   protected <T> T inject(T injectable)
   {
      if (injector == null)
      {
        initializeInjector();
      }
      
      if (injector == null)
      {
         Log.warn("Can't find WeldManager in the servlet context; injection not available for Servlet component: " + injectable);
      }
      else
      {
         injector.inject(injectable);
      }
      return injectable;
   }
   
   protected void release(Object injectable)
   {
      if (injector != null)
      {
         injector.release(injectable);
      }
   }
   
   protected <T extends EventListener> T initializeWeld(T weldListener)
   {
      ((Listener) weldListener).contextInitialized(new ServletContextEvent(ctx));
      return weldListener;
   }
   
   protected void initializeInjector()
   {
      WeldManager manager = (WeldManager) ctx.getAttribute(Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
      if (manager != null)
      {
         injector = new JettyWeldInjector(manager);
      }
   }
   
   private static void appendConfiguration(WebAppContext ctx, Configuration c)
   {
      Configuration[] existing = ctx.getConfigurations();
      Configuration[] updated = new Configuration[existing.length + 1];
      System.arraycopy(existing, 0, updated, 0, existing.length);
      updated[existing.length] = c;
      ctx.setConfigurations(updated);
   }
}
