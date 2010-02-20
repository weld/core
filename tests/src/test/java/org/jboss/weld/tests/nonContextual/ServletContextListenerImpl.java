package org.jboss.weld.tests.nonContextual;

import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServletContextListenerImpl implements ServletContextListener
{
   
   public static boolean ok;
   
   @Inject Logger log;

   public void contextDestroyed(ServletContextEvent sce)
   {
      
   }

   public void contextInitialized(ServletContextEvent sce)
   {
      log.finer("hello!");
      ok = true;
   }

}
