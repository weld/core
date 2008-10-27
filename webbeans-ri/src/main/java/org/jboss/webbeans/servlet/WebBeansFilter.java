package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.contexts.AbstractContext;
import org.jboss.webbeans.contexts.RequestContext;


/**
 * 
 * @author Shane Bryzak
 *
 */
public class WebBeansFilter implements Filter
{
   private Manager container;

   public void init(FilterConfig filterConfig) throws ServletException 
   {
      
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
       throws IOException, ServletException 
   {
      AbstractContext requestContext = new RequestContext();

      try
      {
         container.addContext(requestContext);
         
         chain.doFilter(request, response);
      }
      finally
      {
         requestContext.destroy(container);
      }      
   }

   public void destroy() 
   {
      
   }
}
