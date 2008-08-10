package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.webbeans.Container;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.BasicContext;
import org.jboss.webbeans.init.Initialization;

/**
 * 
 * @author Shane Bryzak
 *
 */
public class WebBeansFilter implements Filter
{
   private Container container;

   public void init(FilterConfig filterConfig) throws ServletException 
   {
      container = (Container) filterConfig.getServletContext().getAttribute(
            Initialization.WEBBEANS_CONTAINER_KEY);      
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
