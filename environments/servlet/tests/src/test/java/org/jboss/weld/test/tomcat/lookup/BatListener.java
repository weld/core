package org.jboss.weld.test.tomcat.lookup;

import org.jboss.weld.servlet.api.ServletListener;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

public class BatListener implements ServletListener
{
   public static final String BAT_ATTRIBUTE_NAME = "batAttribute";

   @Inject Sewer sewer;

   public void contextInitialized(ServletContextEvent sce)
   {
      if (isSewerNameOk()) {
         sce.getServletContext().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void contextDestroyed(ServletContextEvent sce)
   {
      isSewerNameOk();
   }

   public void requestInitialized(ServletRequestEvent sre)
   {
      if (isSewerNameOk()) {
         sre.getServletRequest().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void requestDestroyed(ServletRequestEvent sre)
   {
      isSewerNameOk();
   }

   public void sessionCreated(HttpSessionEvent se)
   {
      if (isSewerNameOk()) {
         se.getSession().setAttribute(BAT_ATTRIBUTE_NAME, Boolean.TRUE);
      }
   }

   public void sessionDestroyed(HttpSessionEvent se)
   {
      isSewerNameOk();
   }

   private boolean isSewerNameOk() throws NullPointerException
   {
      try
      {
         return Sewer.NAME.equals(sewer.getName());
      }
      catch (NullPointerException e)
      {
         e.printStackTrace();
         return false;
      }
   }
}
