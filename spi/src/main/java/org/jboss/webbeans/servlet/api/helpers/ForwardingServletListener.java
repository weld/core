package org.jboss.webbeans.servlet.api.helpers;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.webbeans.servlet.api.ServletListener;

public abstract class ForwardingServletListener implements ServletListener
{
   
   protected abstract ServletListener delegate();

   public void contextDestroyed(ServletContextEvent sce)
   {
      delegate().contextDestroyed(sce);
   }

   public void contextInitialized(ServletContextEvent sce)
   {
      delegate().contextInitialized(sce);
   }

   public void requestDestroyed(ServletRequestEvent sre)
   {
      delegate().requestDestroyed(sre);
   }

   public void requestInitialized(ServletRequestEvent sre)
   {
      delegate().requestInitialized(sre);
   }

   public void sessionCreated(HttpSessionEvent se)
   {
      delegate().sessionCreated(se);
   }

   public void sessionDestroyed(HttpSessionEvent se)
   {
      delegate().sessionDestroyed(se);
   }
   
}
