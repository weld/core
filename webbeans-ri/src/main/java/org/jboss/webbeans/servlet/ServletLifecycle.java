package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.Lifecycle;
import org.jboss.webbeans.contexts.ApplicationContext;
import org.jboss.webbeans.contexts.Contexts;
import org.jboss.webbeans.contexts.RequestContext;
import org.jboss.webbeans.contexts.SessionContext;

public class ServletLifecycle
{
   private static ServletContext servletContext;
   
   public static void beginApplication(ServletContext context)
   {
      servletContext = context;  
      Lifecycle.beginApplication(new ServletApplicationMap(context));
   }
   
   public static void endApplication() {
      Lifecycle.endApplication();
      servletContext = null;
   }
   
   public static void beginSession(HttpSession session)
   {
      Lifecycle.beginSession( new ServletSessionMap(session) );
   }
   
   public static void endSession(HttpSession session) {
      Lifecycle.endSession( new ServletSessionMap(session) );  
   }   
   
   public static void beginRequest(HttpServletRequest request) {
      Contexts.applicationContext.set(new ApplicationContext( Lifecycle.getApplication() ) );
      Contexts.requestContext.set( new RequestContext( new ServletRequestMap(request) ) );
      Contexts.sessionContext.set( new SessionContext( new ServletRequestSessionMap(request) ) );
   }
   
   public static void endRequest(HttpServletRequest request) {
      Lifecycle.clearThreadlocals();
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
}
