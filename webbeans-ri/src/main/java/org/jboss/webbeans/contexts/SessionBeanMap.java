package org.jboss.webbeans.contexts;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

public class SessionBeanMap implements BeanMap
{
   private static final String KEY_PREFIX = "SessionScoped#";

   private HttpSession session;
   private ManagerImpl manager;
   private BeanMap cache;

   public SessionBeanMap(ManagerImpl manager)
   {
      super();
      this.manager = manager;
      cache = new SimpleBeanMap();
   }

   public void setSession(HttpSession session)
   {
      this.session = session;
   }
   
   private void checkSession() {
      if (session == null) {
         throw new IllegalArgumentException("Session has not been initialized in SessionBeanMap");
      }
   }

   @SuppressWarnings("unchecked")
   public <T> T get(Bean<? extends T> bean)
   {
      checkSession();
      T instance = cache.get(bean);
      if (instance != null)
      {
         return instance;
      }
      String id = KEY_PREFIX + manager.getBeans().indexOf(bean);
      instance = (T) session.getAttribute(id);
      if (instance != null)
      {
         cache.put(bean, instance);
      }
      return instance;
   }

   public <T> T remove(Bean<? extends T> bean)
   {
      checkSession();
      T instance = get(bean);
      String id = KEY_PREFIX + manager.getBeans().indexOf(bean);
      session.removeAttribute(id);
      cache.remove(bean);
      return instance;
   }

   @SuppressWarnings("unchecked")
   public void clear()
   {
      checkSession();
      Enumeration names = session.getAttributeNames();
      while (names.hasMoreElements()) {
         String name = (String) names.nextElement();
         session.removeAttribute(name);
      }
      cache.clear();
   }

   @SuppressWarnings("unchecked")
   public Iterable<Bean<? extends Object>> keySet()
   {
      checkSession();

      List<Bean<?>> beans = new ArrayList<Bean<?>>();

      Enumeration names = session.getAttributeNames();
      while (names.hasMoreElements()) {
         String name = (String) names.nextElement();
         if (name.startsWith(KEY_PREFIX)) {
            String id = name.substring(KEY_PREFIX.length());
            Bean<?> bean = manager.getBeans().get(Integer.parseInt(id));
            beans.add(bean);
         }
      }
      
      return beans;
   }

   public <T> T put(Bean<? extends T> bean, T instance)
   {
      checkSession();
      String id = KEY_PREFIX + manager.getBeans().indexOf(bean);
      session.setAttribute(id, instance);
      return cache.put(bean, instance);
   }

}
