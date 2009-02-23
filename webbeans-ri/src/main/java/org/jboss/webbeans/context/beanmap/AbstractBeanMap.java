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
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.EnumerationIterable;
import org.jboss.webbeans.util.Names;

/**
 * Provides common BeanMap operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractBeanMap implements BeanStore
{
   // The log provider
   private static LogProvider log = Logging.getLogProvider(AbstractBeanMap.class);

   /**
    * Gets a bean from the map
    * 
    * @param contextual The bean to get
    * @return The instance
    */
   @SuppressWarnings("unchecked")
   public <T> T get(Contextual<? extends T> contextual)
   {
      String key = getBeanMapAdaptor().getContextualKey(contextual);
      T instance = (T) getAttribute(key);
      log.trace("Looked for " + key + " and got " + instance);
      return instance;
   }

   /**
    * Removes an instance from the map
    * 
    * @param contextual The bean of the instance to remove
    * @return The removed instance
    */
   public <T> T remove(Contextual<? extends T> contextual)
   {
      T instance = get(contextual);
      String key = getBeanMapAdaptor().getContextualKey(contextual);
      removeAttribute(key);
      log.trace("Removed bean under key " + key);
      return instance;
   }

   /**
    * Clears the bean map
    */
   public void clear()
   {
      for (String attributeName : getFilteredAttributeNames())
      {
         removeAttribute(attributeName);
      }
      log.trace("Bean Map cleared");
   }

   /**
    * Returns the beans present in the map
    * 
    * @return The beans
    */
   public Iterable<Contextual<? extends Object>> getBeans()
   {
      List<Contextual<?>> contextuals = new ArrayList<Contextual<?>>();
      BeanMapAdaptor adaptor = getBeanMapAdaptor();
      for (String attributeName : getFilteredAttributeNames())
      {
         int beanIndex = adaptor.getBeanIndexFromKey(attributeName);
         Contextual<?> contextual = CurrentManager.rootManager().getBeans().get(beanIndex);
         contextuals.add(contextual);
      }
      return contextuals;
   }

   /**
    * Gets the list of attribute names that is held by the bean map
    * 
    * @return The list of attribute names
    */
   private List<String> getFilteredAttributeNames()
   {
      List<String> attributeNames = new ArrayList<String>();
      BeanMapAdaptor adaptor = getBeanMapAdaptor();
      for (String attributeName : new EnumerationIterable<String>(getAttributeNames()))
      {
         if (adaptor.acceptKey(attributeName))
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
      String key = getBeanMapAdaptor().getContextualKey(bean);
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
    * Gets an enumeration of the attribute names present in the underlying
    * storage
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
    * Gets an adaptor for handling keys in a bean map
    * 
    * @return The filter
    */
   protected abstract BeanMapAdaptor getBeanMapAdaptor();


   @Override
   public String toString()
   {
      return "holding " + Names.count(getBeans()) + " instances";
   }
}
