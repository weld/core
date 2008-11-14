package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.webbeans.Lifecycle;

public class ServletLifecycle
{
   private static ServletContext servletContext;
   
   public static void beginApplication(ServletContext context)
   {
      servletContext = context;  
   }
   
   public static void endApplication() {
      servletContext = null;
   }
   
   public static void beginSession(HttpSession session)
   {
      Lifecycle.beginSession();
   }
   
   public static void endSession(HttpSession session) {
      Lifecycle.endSession();  
   }   
   
   public static void beginRequest(HttpServletRequest request) {
   }
   
   public static void endRequest(HttpServletRequest request) {
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
}
