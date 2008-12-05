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

package org.jboss.webbeans.servlet;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Contextual;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.contexts.AbstractBeanMapAdaptor;
import org.jboss.webbeans.contexts.SessionContext;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A BeanMap that uses a HTTP session as backing map
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.contexts.SessionContext
 */
public class SessionBeanMap extends AbstractBeanMapAdaptor
{
   private static LogProvider log = Logging.getLogProvider(SessionBeanMap.class);

   // The HTTP session to use as backing map
   private HttpSession session;

   /**
    * Constructor
    * 
    * @param keyPrefix The storage names prefix
    */
   public SessionBeanMap(HttpSession httpSession)
   {
      super();
      this.session = httpSession;
      log.trace("SessionBeanMap created with prefix " + getKeyPrefix());
   }

   /**
    * Gets a bean from the session
    * 
    * It determines an ID for the
    * bean which and looks for it in the session. The bean instance is returned
    * (null if not found in the session).
    * 
    * @param bean The bean to get from the session
    * @return An instance of the bean
    * 
    * @see org.jboss.webbeans.contexts.BeanMap#get(Bean)
    */
   @SuppressWarnings("unchecked")
   public <T> T get(Contextual<? extends T> bean)
   {
      String key = getBeanKey(bean);
      T instance = (T) session.getAttribute(key);
      log.trace("Searched session for key " + key + " and got " + instance);
      return instance;
   }

   /**
    * Removes a bean instance from the session
    * 
    * It determines an ID for the
    * bean and that key is then removed from the session, whether it was present
    * in the first place or not.
    * 
    * @param bean The bean whose instance to remove.
    * @return The instance removed
    * 
    * @see org.jboss.webbeans.contexts.BeanMap#remove(Bean)
    */
   public <T> T remove(Contextual<? extends T> bean)
   {
      T instance = get(bean);
      String key = getBeanKey(bean);
      session.removeAttribute(key);
      log.trace("Removed bean " + bean + " with key " + key + " from session");
      return instance;
   }

   /**
    * Clears the session of any beans.
    * 
    * First, checks that the session is present. Then, iterates over the
    * attribute names in the session and removes them if they start with the
    * know prefix.
    * 
    * @see org.jboss.webbeans.contexts.BeanMap#clear()
    */
   @SuppressWarnings("unchecked")
   public void clear()
   {
      Enumeration names = session.getAttributeNames();
      while (names.hasMoreElements())
      {
         String name = (String) names.nextElement();
         session.removeAttribute(name);
      }
      log.trace("Session cleared");
   }

   /**
    * Gets an iterable over the beans present in the storage.
    * 
    * Iterates over the names in the session. If a name starts with the known
    * prefix, strips it out to get the index to the bean in the manager bean
    * list. Retrieves the bean from that list and puts it in the result-list.
    * Finally, returns the list.
    * 
    * @return An Iterable to the beans in the storage
    * 
    * @see org.jboss.webbeans.contexts.BeanMap#keySet()
    */
   @SuppressWarnings("unchecked")
   public Iterable<Contextual<? extends Object>> keySet()
   {

      List<Contextual<?>> beans = new ArrayList<Contextual<?>>();

      Enumeration names = session.getAttributeNames();
      while (names.hasMoreElements())
      {
         String name = (String) names.nextElement();
         if (name.startsWith(getKeyPrefix()))
         {
            String id = name.substring(getKeyPrefix().length());
            Contextual<?> bean = ManagerImpl.instance().getBeans().get(Integer.parseInt(id));
            beans.add(bean);
         }
      }

      return beans;
   }

   /**
    * Puts a bean instance in the session
    * 
    * Generates a bean map key, puts
    * the instance in the session under that key.
    * 
    * @param bean The bean to use as key
    * @param instance The bean instance to add
    * 
    * @see org.jboss.webbeans.contexts.BeanMap#put(Bean, Object)
    */
   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      String key = getBeanKey(bean);
      session.setAttribute(key, instance);
      log.trace("Stored instance " + instance + " for bean " + bean + " under key " + key + " in session");
   }

   @SuppressWarnings("unchecked")
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      List<Contextual<?>> beans = (List) keySet();
      buffer.append("Bean -> bean instance mappings in HTTP session: " + beans.size() + "\n");
      int i = 0;
      for (Contextual<?> bean : beans)
      {
         Object instance = get(bean);
         buffer.append(++i + " - " + getBeanKey(bean) + ": " + instance + "\n");
      }
      return buffer.toString();
   }

   @Override
   protected String getKeyPrefix()
   {
      return SessionContext.class.getName();
   }

}
