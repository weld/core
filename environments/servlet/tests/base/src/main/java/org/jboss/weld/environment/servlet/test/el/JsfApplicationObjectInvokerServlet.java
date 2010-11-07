package org.jboss.weld.environment.servlet.test.el;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A basic Servlet that looks up and invokes the JSF application object outside
 * the context of the JSF Servlet.
 * 
 * @author Dan Allen <dan.j.allen@gmail.com>
 */
public class JsfApplicationObjectInvokerServlet extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      response.setContentType("text/plain");
      response.getWriter().append(getApplication().getProjectStage().name());
   }

   public static Application getApplication()
   {
       ApplicationFactory factory = (ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY);
       return factory.getApplication();
   }
}
