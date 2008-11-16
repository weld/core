package org.jboss.webbeans.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.contexts.AbstractContext.BeanMap;
import org.jboss.webbeans.util.JNDI;

public class ServletLifecycle
{
   private static ServletContext servletContext;
   private static final String SESSION_BEANMAP_KEY = "org.jboss.webbeans.context.session.data";
   
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
      ManagerImpl manager = (ManagerImpl) JNDI.lookup("manager");
      SessionContext sessionContext = (SessionContext) manager.getContext(SessionScoped.class);
      BeanMap sessionBeans = (BeanMap) request.getAttribute(SESSION_BEANMAP_KEY);
      sessionContext.setBeans(sessionBeans);
   }
   
   public static void endRequest(HttpServletRequest request) {
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
}
