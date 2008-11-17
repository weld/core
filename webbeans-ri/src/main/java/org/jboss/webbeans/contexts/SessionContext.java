package org.jboss.webbeans.contexts;

import javax.webbeans.SessionScoped;

import org.jboss.webbeans.ManagerImpl;

public class SessionContext extends PrivateContext {

   private ThreadLocal<SessionBeanMap> beans;
   
   public SessionContext(ManagerImpl manager)
   {
      super(SessionScoped.class);
      beans = new ThreadLocal<SessionBeanMap>();
      beans.set(new SessionBeanMap(manager));
   }
   
   @Override
   public BeanMap getBeanMap()
   {
      return beans.get();
   }
   
   @Override
   public String toString()
   {
      return "Session context";
   }   
}
