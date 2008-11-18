package org.jboss.webbeans.contexts;

import javax.servlet.http.HttpSession;
import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;

/**
 * The session context
 * 
 * @author Nicklas Karlsson
 */
public class SessionContext extends PrivateContext {

   public SessionContext(ManagerImpl manager)
   {
      super(SessionScoped.class);
      // Replaces the BeanMap implementation with a session-based one
      beans.set(new SessionBeanMap(manager));
   }
   
   @Override
   public String toString()
   {
      return "Session context";
   }   
   
   /**
    * Sets the session in the session bean map
    * 
    * @param session The session to set
    */
   public void setSession(HttpSession session) {
      ((SessionBeanMap)getBeanMap()).setSession(session);
   }
}
