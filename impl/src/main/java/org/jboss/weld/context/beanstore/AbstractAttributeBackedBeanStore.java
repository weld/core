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
package org.jboss.weld.context.beanstore;

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_INSTANCE_ADDED;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_INSTANCE_FOUND;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXT_CLEARED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.collections.EnumerationList;
import org.slf4j.cal10n.LocLogger;

/**
 * Provides common BeanStore operations
 * 
 * @author Nicklas Karlsson
 * 
 */
public abstract class AbstractAttributeBackedBeanStore implements BeanStore
{
   private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

   /**
    * Gets a bean from the store
    * 
    * @param contextual The bean to get
    * @return The instance
    */
   @SuppressWarnings("unchecked")
   public <T> ContextualInstance<T> get(String id)
   {
      String key = getNamingScheme().getKey(id);
      ContextualInstance<T> instance = (ContextualInstance<T>) getAttribute(key);
      log.trace(CONTEXTUAL_INSTANCE_FOUND, id, instance, this);
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
      log.trace(CONTEXT_CLEARED, this);
   }

   /**
    * Returns the beans present in the store
    * 
    * @return The beans
    */
   public Collection<String> getContextualIds()
   {
      List<String> contextuals = new ArrayList<String>();
      NamingScheme namingScheme = getNamingScheme();
      for (String attributeName : getFilteredAttributeNames())
      {
         String id = namingScheme.getId(attributeName);
         contextuals.add(id);
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
      NamingScheme namingScheme = getNamingScheme();
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
   public <T> void put(String id, ContextualInstance<T> beanInstance)
   {
      String key = getNamingScheme().getKey(id);
      setAttribute(key, beanInstance);
      log.trace(CONTEXTUAL_INSTANCE_ADDED, beanInstance.getContextual(), key, this);
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
   protected abstract NamingScheme getNamingScheme();


   @Override
   public String toString()
   {
      return "holding " + Names.count(getContextualIds()) + " instances";
   }
}
