package org.jboss.webbeans;

import java.util.Map;

import javax.webbeans.ApplicationScoped;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.contexts.Contexts;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.servlet.ServletApplicationMap;
import org.jboss.webbeans.servlet.ServletSessionMap;

public class Lifecycle
{
   private static ServletApplicationMap application;
   
   public static void beginApplication(ServletApplicationMap serverApplicationMap)
   {
      application = serverApplicationMap;
   }
   
   public static Map<String, Object> getApplication() 
   {
      return application;
   }   

   public static void endApplication()
   {
      Contexts.destroyContext(ApplicationScoped.class);
      Contexts.applicationContext.set(null);
      application = null;
   }

   public static void beginSession(ServletSessionMap servletSessionMap)
   {
      Contexts.sessionContext.set(new SessionContext(servletSessionMap));
   }

   public static void endSession(ServletSessionMap servletSessionMap)
   {
      Contexts.destroyContext(SessionScoped.class);
      Contexts.sessionContext.set(null);
   }
   
   public static void clearThreadlocals() 
   {
      Contexts.applicationContext.set(null);
      Contexts.sessionContext.set(null);
      Contexts.requestContext.set(null);
   }   

}
