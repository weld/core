package org.jboss.webbeans.servlet;

import org.jboss.webbeans.util.ApiAbstraction;

public class Servlet extends ApiAbstraction
{
   
   public static final Class<?> SERVLET_CLASS = classForName("javax.servlet.Servlet");
   public static final Class<?> FILTER_CLASS = classForName("javax.servlet.Filter");
   public static final Class<?> SERVLET_CONTEXT_LISTENER_CLASS = classForName("javax.servlet.ServletContextListener");
   public static final Class<?> HTTP_SESSION_LISTENER_CLASS = classForName("javax.servlet.http.HttpSessionListener");
   public static final Class<?> SERVLET_REQUEST_LISTENER_CLASS = classForName("javax.servlet.ServletRequestListener");
   
}
