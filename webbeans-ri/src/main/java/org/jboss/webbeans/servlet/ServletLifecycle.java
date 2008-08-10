package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;


/**
 * 
 * @author Shane Bryzak
 *
 */
public class ServletLifecycle
{
   private static ServletContext servletContext;
   
   public static void beginApplication(ServletContext context)
   {
      servletContext = context;  
   }
   
   public static void beginSession(HttpSession session)
   {
      // TODO
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
}
