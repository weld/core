package org.jboss.webbeans.servlet.api.helpers;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.webbeans.servlet.api.ServletListener;

/**
 * No-op implementation of ServletListener
 * 
 * @author Pete Muir
 *
 */
public class AbstractServletListener implements ServletListener
{
   
   public void contextDestroyed(ServletContextEvent sce) {}
   
   public void contextInitialized(ServletContextEvent sce) {}
   
   public void requestDestroyed(ServletRequestEvent sre) {}
   
   public void requestInitialized(ServletRequestEvent sre) {}
   
   public void sessionCreated(HttpSessionEvent se) {}
   
   public void sessionDestroyed(HttpSessionEvent se) {}
   
}
