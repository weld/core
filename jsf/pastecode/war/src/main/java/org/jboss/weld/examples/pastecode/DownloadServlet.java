/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
