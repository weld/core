package org.jboss.webbeans.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class WebBeansListener implements ServletContextListener, HttpSessionListener
{

   public void contextInitialized(ServletContextEvent event) 
   {
      ServletLifecycle.beginApplication(event.getServletContext());
   }

   public void sessionCreated(HttpSessionEvent event) 
   {
      ServletLifecycle.beginSession(event.getSession());
   }

   public void sessionDestroyed(HttpSessionEvent event) 
   {
      ServletLifecycle.endSession(event.getSession());
   }

   public void contextDestroyed(ServletContextEvent event) 
   {
      ServletLifecycle.endApplication();
   }

}
