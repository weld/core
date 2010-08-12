package org.jboss.weld.environment.servlet.test.tomcat.lookup;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.servlet.api.ServletListener;

public class BatListener implements ServletListener
{
   public static final String BAT_ATTRIBUTE_NAME = "batAttribute";

   @Inject
   Sewer sewer;

   public void contextInitialized(ServletContextEvent sce)
   {
      if (isSewerNameOk())
      {
         sce.getServletContext().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void contextDestroyed(ServletContextEvent sce)
   {
      isSewerNameOk();
   }

   public void requestInitialized(ServletRequestEvent sre)
   {
      if (isSewerNameOk())
      {
         sre.getServletRequest().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void requestDestroyed(ServletRequestEvent sre)
   {
      isSewerNameOk();
   }

   public void sessionCreated(HttpSessionEvent se)
   {
      if (isSewerNameOk())
      {
         se.getSession().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void sessionDestroyed(HttpSessionEvent se)
   {
      isSewerNameOk();
   }

   private boolean isSewerNameOk() throws NullPointerException
   {
      return sewer != null && Sewer.NAME.equals(sewer.getName());
   }
}
