package org.jboss.weld.environment.servlet.test.tomcat.lookup;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BatServlet extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setStatus(getStatus(req));
   }

   private int getStatus(HttpServletRequest req)
   {
      return  req.getAttribute(BatListener.BAT_ATTRIBUTE_NAME) == Boolean.TRUE &&
              req.getSession().getAttribute(BatListener.BAT_ATTRIBUTE_NAME) == Boolean.TRUE &&
              req.getSession().getServletContext().getAttribute(BatListener.BAT_ATTRIBUTE_NAME) == Boolean.TRUE
              ? HttpServletResponse.SC_OK
              : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
   }

}
