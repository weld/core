package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.contexts.SessionBeanMap;
import org.jboss.webbeans.contexts.SessionContext;

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
   }
   
   public static void endSession(HttpSession session) {
   }   
   
   public static void beginRequest(HttpServletRequest request) {
      ManagerImpl manager = new ManagerImpl();
      SessionContext sessionContext = (SessionContext) manager.getContext(SessionScoped.class);
      SessionBeanMap sessionBeanMap = (SessionBeanMap) sessionContext.getBeanMap();
      sessionBeanMap.setSession(request.getSession(true));
   }
   
   public static void endRequest(HttpServletRequest request) {
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
}
