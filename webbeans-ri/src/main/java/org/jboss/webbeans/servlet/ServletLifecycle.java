package org.jboss.webbeans.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bootstrap.Bootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.JNDI;
import org.jboss.webbeans.util.Reflections;

public class ServletLifecycle
{
   
   private static LogProvider log = Logging.getLogProvider(ServletLifecycle.class);
   
   private static final String MANAGER_JNDI_KEY = "java:comp/Manager";
   
   private static ServletContext servletContext;
   
   public static void beginApplication(ServletContext context)
   {
      servletContext = context;
      Bootstrap bootstrap = new Bootstrap();
      bootstrap.boot(getWebBeanDiscovery());
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
      ManagerImpl manager = (ManagerImpl) JNDI.lookup(MANAGER_JNDI_KEY);
      SessionContext sessionContext = (SessionContext) manager.getContext(SessionScoped.class);
      sessionContext.setSession(request.getSession(true));
   }
   
   public static void endRequest(HttpServletRequest request) {
   }
   
   public static ServletContext getServletContext() 
   {
      return servletContext;
   }
   
   // TODO move some of this bootstrap for reuse outside Servlet
   private static WebBeanDiscovery getWebBeanDiscovery()
   {
      WebBeanDiscovery webBeanDiscovery = null;

      for (Class<? extends WebBeanDiscovery> clazz : Bootstrap.getWebBeanDiscoveryClasses())
      {
         Constructor<? extends WebBeanDiscovery> constructor = Reflections.getConstructor(clazz, ServletContext.class);
         if (constructor != null)
         {
            try
            {
               webBeanDiscovery = constructor.newInstance(servletContext);
               break;
            }
            catch (InstantiationException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (IllegalAccessException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (IllegalArgumentException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
            catch (InvocationTargetException e)
            {
               log.warn("Error creating WebBeanDiscovery provider" + clazz.getName(), e);
            }
         }
      }
      return webBeanDiscovery;
   }
   
}
