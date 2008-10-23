package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.webbeans.RequestScoped;
import javax.webbeans.manager.Manager;

import org.jboss.webbeans.BasicContext;

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
      BasicContext requestContext = new BasicContext(RequestScoped.class);

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
