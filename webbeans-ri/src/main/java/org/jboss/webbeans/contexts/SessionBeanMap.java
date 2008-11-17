package org.jboss.webbeans.contexts;

import javax.servlet.http.HttpSession;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

public class SessionBeanMap extends BeanMap
{
   private HttpSession session;
   private ManagerImpl manager;
   
   public SessionBeanMap(ManagerImpl manager) {
      super();
      this.manager = manager;
   }

   public void setSession(HttpSession session) {
      this.session = session;
   }
   
   @Override
   public <T> T get(Bean<? extends T> bean)
   {
      String id = Integer.toString(manager.getBeans().indexOf(bean));
      T instance = super.get(bean);
      session.setAttribute(id, instance);
      return instance;
   }

   @Override
   public <T> T remove(Bean<? extends T> bean)
   {
      T instance = super.remove(bean);
      String id = Integer.toString(manager.getBeans().indexOf(bean));
      session.removeAttribute(id);
      return instance;
   }

}
