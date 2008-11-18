/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,  
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.jboss.webbeans.contexts;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.ManagerImpl;

/**
 * A BeanMap that uses a HTTP session as backing map
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.contexts.SessionContext
 */
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
      // A "normal" BeanMap is used as cache
      cache = new SimpleBeanMap();
   }

   /**
    * The SessionBeanMap requires a HTTP session to work. It is created without one
    * so this method must be called before it can be operated upon
    * 
    * @param session The session to use as a backing map
    */
   public void setSession(HttpSession session)
   {
      this.session = session;
   }
   
   /**
    * Used to check if the session has been set and throws an exception if it's null.
    */
   private void checkSession() {
      if (session == null) {
         throw new IllegalArgumentException("Session has not been initialized in SessionBeanMap");
      }
   }

   /**
    * Returns a map key to a bean. Uses a known prefix and appends the index of the Bean
    * in the Manager bean list.
    * 
    * @param bean The bean to generate a key for.
    * 
    * @return A unique key;
    */
   @SuppressWarnings("unused")
   private String getBeanKey(Bean<?> bean) {
      // TODO Append scope to in order to make class usable by multiple contexts
      return KEY_PREFIX + manager.getBeans().indexOf(bean);
   }
   
   /**
    * Gets a bean from the session
    * 
    * First, checks that the session is present. Then tries to get the instance from the cache and
    * return it if found. It determines an ID for the bean which and looks for it in the session. 
    * If the instance is found in, it is added to the cache. The bean instance is returned (null 
    * if not found in the session).
    * 
    * @param bean The bean to get from the session 
    */
   @SuppressWarnings("unchecked")
   public <T> T get(Bean<? extends T> bean)
   {
      checkSession();
      T instance = cache.get(bean);
      if (instance != null)
      {
         return instance;
      }
      String id = getBeanKey(bean);
      instance = (T) session.getAttribute(id);
      if (instance != null)
      {
         cache.put(bean, instance);
      }
      return instance;
   }

   /**
    * Removes a bean instance from the session
    * 
    * First, checks that the session is present. Then, tries to get the bean instance from the cache.
    * It determines an ID for the bean and that key is then removed from the session and the cache, whether
    * they were present in the first place or not.
    * 
    * @param bean The bean whose instance to remove.
    */
   public <T> T remove(Bean<? extends T> bean)
   {
      checkSession();
      T instance = get(bean);
      String id = getBeanKey(bean);
      session.removeAttribute(id);
      cache.remove(bean);
      return instance;
   }

   /**
    * Clears the session of any beans. 
    * 
    * First, checks that the session is present. Then, iterates
    * over the attribute names in the session and removes them if they start with the know prefix.
    * Finally, clears the cache.
    */
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

   /**
    * Gets an iterable over the beans present in the storage. 
    * 
    * Iterates over the names
    * in the session. If a name starts with the known prefix, strips it out to get the 
    * index to the bean in the manager bean list. Retrieves the bean from that list and
    * puts it in the result-list. Finally, returns the list. 
    *  
    * @return An Iterable to the beans in the storage
    */
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

   /**
    * Puts a bean instance in the session
    * 
    * First, checks that the session is present. Generates a bean map key, puts the instance in the 
    * session under that key and adds the bean instance to the cache.
    * 
    * @param bean The bean to use as key
    * 
    * @param instance The bean instance to add
    * 
    * @return The instance added
    */
   public <T> T put(Bean<? extends T> bean, T instance)
   {
      checkSession();
      String id = getBeanKey(bean);
      session.setAttribute(id, instance);
      return cache.put(bean, instance);
   }

}
