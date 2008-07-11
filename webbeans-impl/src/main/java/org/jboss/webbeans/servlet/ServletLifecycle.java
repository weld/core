package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;

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
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }   
}
