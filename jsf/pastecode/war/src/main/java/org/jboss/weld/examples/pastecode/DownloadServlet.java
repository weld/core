package org.jboss.weld.examples.pastecode;

import java.io.*;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.weld.examples.pastecode.model.Code;
import org.jboss.weld.examples.pastecode.session.CodeEAO;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

public class DownloadServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   @Inject
   Instance<CodeEAO> eaoIn;
   CodeEAO eao;

   public DownloadServlet()
   {
   }

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {

      this.eao = eaoIn.get();
      String id = (String) request.getParameter("id");
      Code c = eao.getCode(id);
      String fileName = c.getUser() + "." + c.getLanguage();
      String txt = c.getText();

      response.setContentType("text/plain");
      response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
      response.setContentLength(txt.length());

      ServletOutputStream out = response.getOutputStream();
      try
      {
         out.print(txt);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         out.close();
      }

   }
}
