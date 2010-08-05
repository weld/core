package org.jboss.weld.test.tomcat.lookup;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CatFilter implements Filter
{

   @Inject Sewer sewer;

   public void init(FilterConfig filterConfig) throws ServletException
   {
      isSewerNameOk();
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      ((HttpServletResponse)response).setStatus(isSewerNameOk() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
   }

   public void destroy()
   {
      isSewerNameOk();
   }

   private boolean isSewerNameOk() throws NullPointerException
   {
      return Sewer.NAME.equals(sewer.getName());
   }
}