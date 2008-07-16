package org.jboss.webbeans.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class WebBeansListener implements ServletContextListener, HttpSessionListener
{


   public void contextInitialized(ServletContextEvent event) 
   {
      ServletLifecycle.beginApplication( event.getServletContext() );
   }

   public void sessionCreated(HttpSessionEvent event) 
   {
      ServletLifecycle.beginSession( event.getSession() );
   }

   public void sessionDestroyed(HttpSessionEvent event) 
   {
      // TODO Auto-generated method stub

   }

   public void contextDestroyed(ServletContextEvent event) 
   {
      // TODO Auto-generated method stub
      
   }

}
