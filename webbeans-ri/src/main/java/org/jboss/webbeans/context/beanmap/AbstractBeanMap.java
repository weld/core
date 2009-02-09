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

package org.jboss.webbeans.context.beanmap;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.context.Contextual;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ApplicationBeanMap;
import org.jboss.webbeans.util.EnumerationIterable;
import org.jboss.webbeans.util.Names;

/**
 * Provides common BeanMap operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractBeanMap implements BeanMap
{
   // The log provider
   private static LogProvider log = Logging.getLogProvider(ApplicationBeanMap.class);

   /**
    * Gets a bean from the map
    * 
    * @param bean The bean to get
    * @return The instance
    */
   @SuppressWarnings("unchecked")
   public <T> T get(Contextual<? extends T> bean)
   {
      String key = getBeanKey(bean);
      T instance = (T) getAttribute(key);
      log.trace("Looked for " + key + " and got " + instance);
      return instance;
   }

   /**
    * Removes an instance from the map
    * 
    * @param bean The bean of the instance to remove
    * @return The removed instance
    */
   public <T> T remove(Contextual<? extends T> bean)
   {
      T instance = get(bean);
      String key = getBeanKey(bean);
      removeAttribute(key);
      log.trace("Removed bean under key " + key);
      return instance;
   }

   /**
    * Clears the bean map
    */
   public void clear()
   {
      for (String name : getFilteredAttributeNames())
      {
         removeAttribute(name);
      }
      log.trace("Bean Map cleared");
   }

   /**
    * Returns the beans present in the map
    * 
    * @return The beans
    */
   public Iterable<Contextual<? extends Object>> keySet()
   {
      List<Contextual<?>> beans = new ArrayList<Contextual<?>>();
      for (String name : getFilteredAttributeNames())
      {
         String id = name.substring(getKeyPrefix().length() + 1);
         Contextual<?> bean = CurrentManager.rootManager().getBeans().get(Integer.parseInt(id));
         beans.add(bean);
      }
      return beans;
   }

   /**
    * Gets the list of attribute names that is held by the bean map
    * 
    * @return The list of attribute names
    */
   private List<String> getFilteredAttributeNames()
   {
      List<String> attributeNames = new ArrayList<String>();
      for (String attributeName : new EnumerationIterable<String>(getAttributeNames()))
      {
         if (attributeName.startsWith(getKeyPrefix()))
         {
            attributeNames.add(attributeName);
         }
      }
      return attributeNames;
   }

   /**
    * Puts an instance of a bean in the map
    * 
    * @param bean The key bean
    * @param instance The instance
    * @return The instance added
    */
   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      String key = getBeanKey(bean);
      setAttribute(key, instance);
      log.trace("Added bean " + bean + " under key " + key);
   }

   /**
    * Gets an attribute from the underlying storage
    * 
    * @param key The key of the attribute
    * @return The data
    */
   protected abstract Object getAttribute(String key);

   /**
    * Removes an attribute from the underlying storage
    * 
    * @param key The attribute to remove
    */
   protected abstract void removeAttribute(String key);

   /**
    * Gets an enumeration of the attribute names present in the underlying storage
    * 
    * @return The attribute names
    */
   protected abstract Enumeration<String> getAttributeNames();

   /**
    * Sets an instance under a key in the underlying storage
    * 
    * @param key The key
    * @param instance The instance
    */
   protected abstract void setAttribute(String key, Object instance);

   /**
    * Gets a key prefix that should be prefixed to names
    * 
    * @return The prefix
    */
   protected abstract String getKeyPrefix();

   /**
    * Returns a map key to a bean. Uses a known prefix and appends the index of
    * the Bean in the Manager bean list.
    * 
    * @param bean The bean to generate a key for.
    * @return A unique key;
    */
   protected String getBeanKey(Contextual<?> bean)
   {
      return getKeyPrefix() + "#" + CurrentManager.rootManager().getBeans().indexOf(bean);
   }

   @Override
   public String toString()
   {
      return "holding " + Names.count(keySet()) + " instances under the key prefix " + getKeyPrefix();
   }
}
