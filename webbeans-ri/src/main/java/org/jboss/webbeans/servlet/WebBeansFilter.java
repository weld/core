package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Shane Bryzak
 * 
 */
public class WebBeansFilter implements Filter
{
   public void init(FilterConfig filterConfig) throws ServletException
   {

   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      try
      {
         ServletLifecycle.beginRequest((HttpServletRequest) request);
         chain.doFilter(request, response);
      }
      finally
      {
         ServletLifecycle.endRequest((HttpServletRequest) request);
      }
   }

   public void destroy()
   {
   }
}
