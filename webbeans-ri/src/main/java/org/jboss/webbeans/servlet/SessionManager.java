package org.jboss.webbeans.servlet;

import javax.context.RequestScoped;
import javax.inject.Produces;
import javax.servlet.http.HttpSession;

@RequestScoped
public class SessionManager
{
   private HttpSession session;

   public void setSession(HttpSession session)
   {
      this.session = session;
   }

   @Produces
   @RequestScoped
   HttpSession produceSession()
   {
      return session;
   }

}
