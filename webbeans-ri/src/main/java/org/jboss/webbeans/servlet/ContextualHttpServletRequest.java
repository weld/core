package org.jboss.webbeans.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


public abstract class ContextualHttpServletRequest
{
   private final HttpServletRequest request;

   public ContextualHttpServletRequest(HttpServletRequest request)
   {
      this.request = request;
   }

   public abstract void process() throws Exception;

   public void run() throws ServletException, IOException
   {
      ServletLifecycle.beginRequest(request);

      if (request.getSession(false) == null)
      {
         request.getSession(true);
      }

      try
      {
         process();
      } catch (Exception e)
      {
         throw new IOException(e);
      } finally
      {
         ServletLifecycle.endRequest(request);
      }
   }

}
