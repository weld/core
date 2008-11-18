package org.jboss.webbeans.contexts;

import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;

public class SessionContext extends PrivateContext {

   public SessionContext(ManagerImpl manager)
   {
      super(SessionScoped.class);
      beans.set(new SessionBeanMap(manager));
   }
   
   @Override
   public String toString()
   {
      return "Session context";
   }   
   
   public void setSession(HttpSession session) {
      ((SessionBeanMap)getBeanMap()).setSession(session);
   }
}
