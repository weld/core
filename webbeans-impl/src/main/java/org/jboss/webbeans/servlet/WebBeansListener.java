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

   public void contextDestroyed(ServletContextEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   public void contextInitialized(ServletContextEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   public void sessionCreated(HttpSessionEvent arg0) {
      // TODO Auto-generated method stub
      
   }

   public void sessionDestroyed(HttpSessionEvent arg0) {
      // TODO Auto-generated method stub
      
   }

}
