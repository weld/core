package org.jboss.weld.environment.servlet.test.injection;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class BatListener implements ServletContextListener, HttpSessionListener, ServletRequestListener
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
