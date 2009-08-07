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
package org.jboss.webbeans.context.beanstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.enterprise.context.spi.Contextual;

import org.jboss.webbeans.ContextualIdStore;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.ContexutalInstance;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Names;
import org.jboss.webbeans.util.collections.EnumerationList;

/**
 * Provides common BeanStore operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractAttributeBackedBeanStore implements BeanStore
{
   // The log provider
   private static LogProvider log = Logging.getLogProvider(AbstractAttributeBackedBeanStore.class);

   /**
    * Gets a bean from the store
    * 
    * @param contextual The bean to get
    * @return The instance
    */
   @SuppressWarnings("unchecked")
   public <T> ContexutalInstance<T> get(Contextual<? extends T> contextual)
   {
      Integer contextualId = CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getId(contextual);
      String key = getNamingScheme().getKeyFromId(contextualId);
      ContexutalInstance<T> instance = (ContexutalInstance<T>) getAttribute(key);
      log.trace("Looked for " + key + " and got " + instance);
      return instance;
   }

   /**
    * Removes an instance from the store
    * 
    * @param contextual The bean of the instance to remove
    * @return The removed instance
    */
   public <T> T remove(Contextual<? extends T> contextual)
   {
      Integer contextualId = CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getId(contextual);
      T instance = get(contextual).getInstance();
      String key = getNamingScheme().getKeyFromId(contextualId);
      removeAttribute(key);
      log.trace("Removed bean under key " + key);
      return instance;
   }

   /**
    * Clears the bean store
    */
   public void clear()
   {
      for (String attributeName : getFilteredAttributeNames())
      {
         removeAttribute(attributeName);
      }
      log.trace("Bean store cleared");
   }

   /**
    * Returns the beans present in the store
    * 
    * @return The beans
    */
   public Collection<Contextual<? extends Object>> getContextuals()
   {
      List<Contextual<?>> contextuals = new ArrayList<Contextual<?>>();
      BeanStoreNamingScheme namingScheme = getNamingScheme();
      for (String attributeName : getFilteredAttributeNames())
      {
         Integer id = namingScheme.getIdFromKey(attributeName);
         Contextual<?> contextual = CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getContextual(id);
         contextuals.add(contextual);
      }
      return contextuals;
   }

   /**
    * Gets the list of attribute names that is held by the bean store
    * 
    * @return The list of attribute names
    */
   private List<String> getFilteredAttributeNames()
   {
      List<String> attributeNames = new ArrayList<String>();
      BeanStoreNamingScheme namingScheme = getNamingScheme();
      for (String attributeName : new EnumerationList<String>(getAttributeNames()))
      {
         if (namingScheme.acceptKey(attributeName))
         {
            attributeNames.add(attributeName);
         }
      }
      return attributeNames;
   }

   /**
    * Puts an instance of a bean in the store
    * 
    * @param bean The key bean
    * @param instance The instance
    * @return The instance added
    */
   public <T> void put(ContexutalInstance<T> beanInstance)
   {
      Integer contextualId = CurrentManager.rootManager().getServices().get(ContextualIdStore.class).getId(beanInstance.getContextual());
      String key = getNamingScheme().getKeyFromId(contextualId);
      setAttribute(key, beanInstance);
      log.trace("Added Contextual type " + beanInstance.getContextual() + " under key " + key);
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
    * Gets an naming scheme for handling keys in a bean store
    * 
    * @return The naming scheme
    */
   protected abstract BeanStoreNamingScheme getNamingScheme();


   @Override
   public String toString()
   {
      return "holding " + Names.count(getContextuals()) + " instances";
   }
}
