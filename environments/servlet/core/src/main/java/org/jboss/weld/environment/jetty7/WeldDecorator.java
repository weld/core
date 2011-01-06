package org.jboss.weld.environment.jetty7;


import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;
import org.jboss.weld.environment.jetty.JettyWeldInjector;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.manager.api.WeldManager;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import java.util.EventListener;

/**
 * Jetty 7 ServletContextHandler Decorator that performs CDI injections and cleanup for
 * Servlet, Filter and Listener components.
 * (requires Jetty 7.2 or higher)
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
 * @author <a href="mailto:ben@bulletproof.com.au">Ben Sommerville</a>
 */
public class WeldDecorator implements ServletContextHandler.Decorator
{

   private ServletContext sco;
   private JettyWeldInjector injector;

   public WeldDecorator(ServletContext sco)
   {
      this.sco = sco;
   }

   public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException
   {
      return inject(filter);
   }

   public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException
   {
      return inject(servlet);
   }

   public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException
   {
      // preemptively invoke the Weld Listener
      if (listener instanceof Listener)
      {
         return initializeWeld(listener);
      } else
      {
         return inject(listener);
      }
   }

   public void decorateFilterHolder(FilterHolder filter) throws ServletException
   {

   }

   public void decorateServletHolder(ServletHolder servlet) throws ServletException
   {

   }

   public void destroyServletInstance(Servlet s)
   {
      release(s);
   }

   public void destroyFilterInstance(Filter f)
   {
      release(f);
   }

   public void destroyListenerInstance(EventListener f)
   {
      release(f);
   }

   /**
    * Release all injections created by this decorator
    */
   public void deconfigure()
   {
      if (injector != null)
      {
         injector.releaseAll();
      }
   }


   protected <T> T inject(T injectable)
   {
      if (injector == null)
      {
         initializeInjector();
      }

      if (injector == null)
      {
         Log.warn("Can't find WeldManager in the servlet context; injection is not available for Servlet component: " + injectable);
      } else
      {
         injector.inject(injectable);
      }
      return injectable;
   }

   protected void initializeInjector()
   {
      WeldManager manager = (WeldManager) sco.getAttribute(org.jboss.weld.environment.servlet.Listener.BEAN_MANAGER_ATTRIBUTE_NAME);
      if (manager != null)
      {
         injector = new JettyWeldInjector(manager);
      }
   }

   protected <T extends EventListener> T initializeWeld(T weldListener)
   {
      ((Listener) weldListener).contextInitialized(new ServletContextEvent(sco));
      return weldListener;
   }

   protected void release(Object injectable)
   {
      if (injector != null)
      {
         injector.release(injectable);
      }
   }


}
