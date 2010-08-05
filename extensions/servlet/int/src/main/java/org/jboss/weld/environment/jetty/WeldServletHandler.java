package org.jboss.weld.environment.jetty;

import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;

import javax.servlet.Servlet;
import javax.servlet.Filter;
import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:matija.mazi@gmail.com">Matija Mazi</a>
*/
public class WeldServletHandler extends ServletHandler
{
   private ServletContext sco;
   private JettyWeldInjector injector;

   public WeldServletHandler(ServletHandler existingHandler, ServletContext servletContext)
   {
      sco = servletContext;
      setFilters(existingHandler.getFilters());
      setFilterMappings(existingHandler.getFilterMappings());
      setServlets(existingHandler.getServlets());
      setServletMappings(existingHandler.getServletMappings());
   }

   @Override
   public Servlet customizeServlet(Servlet servlet) throws Exception
   {
      inject(servlet);
      return servlet;
   }

   @Override
   public Filter customizeFilter(Filter filter) throws Exception
   {
      inject(filter);
      return filter;
   }

   protected void inject(Object injectable) {
      if (injector == null)
      {
         injector = (JettyWeldInjector) sco.getAttribute(org.jboss.weld.environment.servlet.Listener.INJECTOR_ATTRIBUTE_NAME);
      }
      if (injector == null)
      {
         Log.warn("Can't find Injector in the servlet context so injection is not available for " + injectable);
      }
      else
      {
         injector.inject(injectable);
      }
   }

   public static void process(WebAppContext wac)
   {
      WeldServletHandler wHanlder = new WeldServletHandler(wac.getServletHandler(), wac.getServletContext());
      wac.setServletHandler(wHanlder);
      wac.getSecurityHandler().setHandler(wHanlder);
   }
}
