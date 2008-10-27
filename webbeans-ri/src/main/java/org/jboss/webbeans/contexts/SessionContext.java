package org.jboss.webbeans.contexts;

import javax.webbeans.SessionScoped;

public class SessionContext extends NormalContext {

   public SessionContext()
   {
      super(SessionScoped.class);
   }

   @Override
   public String toString()
   {
      return "Session context";
   }
}
