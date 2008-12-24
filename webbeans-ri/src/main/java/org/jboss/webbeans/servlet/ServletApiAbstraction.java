package org.jboss.webbeans.servlet;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.util.ApiAbstraction;

public class ServletApiAbstraction extends ApiAbstraction
{
   
   public final Class<?> SERVLET_CLASS;
   public final Class<?> FILTER_CLASS;
   public final Class<?> SERVLET_CONTEXT_LISTENER_CLASS;
   public final Class<?> HTTP_SESSION_LISTENER_CLASS;
   public final Class<?> SERVLET_REQUEST_LISTENER_CLASS;
   
   public ServletApiAbstraction(ResourceLoader resourceLoader)
   {
      super(resourceLoader);
      SERVLET_CLASS = classForName("javax.servlet.Servlet");
      FILTER_CLASS = classForName("javax.servlet.Filter");
      SERVLET_CONTEXT_LISTENER_CLASS = classForName("javax.servlet.ServletContextListener");
      HTTP_SESSION_LISTENER_CLASS = classForName("javax.servlet.http.HttpSessionListener");
      SERVLET_REQUEST_LISTENER_CLASS = classForName("javax.servlet.ServletRequestListener");
   }
   
   
}
