package org.jboss.webbeans.contexts;

import java.util.Map;

import javax.webbeans.SessionScoped;

public class SessionContext extends NormalContext {

   public SessionContext()
   {
      super(SessionScoped.class);
   }

   public SessionContext(Map<String, Object> data)
   {
      super(SessionScoped.class, data);
   }   
   
   @Override
   public String toString()
   {
      return "Session context";
   }
}
