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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.context.Contextual;
import javax.inject.manager.Bean;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

import com.google.common.collect.ForwardingMap;

/**
 * A BeanMap that uses a simple forwarding HashMap as backing map
 * 
 * @author Nicklas Karlsson
 */
public class SimpleBeanMap extends ForwardingMap<Contextual<? extends Object>, Object> implements BeanStore
{
   private static LogProvider log = Logging.getLogProvider(SimpleBeanMap.class);
   
   // The backing map
   protected Map<Contextual<? extends Object>, Object> delegate;

   /**
    * Constructor
    */
   public SimpleBeanMap()
   {
      delegate = new ConcurrentHashMap<Contextual<? extends Object>, Object>();
   }

   /**
    * Gets an instance from the map
    * 
    * @param The bean to look for
    * @return An instance, if found
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#get(Bean)
    */
   public <T extends Object> T get(Contextual<? extends T> bean)
   {
      @SuppressWarnings("unchecked")
      T instance = (T) super.get(bean);
      log.trace("Searched bean map for " + bean + " and got " + instance);
      return instance;
   }

   /**
    * Gets the delegate for the map
    * 
    * @return The delegate
    */
   @Override
   public Map<Contextual<? extends Object>, Object> delegate()
   {
      return delegate;
   }

   /**
    * Removed a instance from the map
    * 
    * @param bean the bean to remove
    * @return The instance removed
    *
    * @see org.jboss.webbeans.context.api.BeanStore#remove(Bean)
    */
   public <T extends Object> T remove(Contextual<? extends T> bean)
   {
      @SuppressWarnings("unchecked")
      T instance = (T) super.remove(bean);
      log.trace("Removed instace " + instance + " for bean " + bean + " from the bean map");
      return instance;
   }

   /**
    * Clears the map
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#clear()
    */
   public void clear()
   {
      delegate.clear();
      log.trace("Bean map cleared");
   }

   /**
    * Returns the beans contained in the map
    * 
    * @return The beans present
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#getBeans()
    */
   public Set<Contextual<? extends Object>> getBeans()
   {
      return delegate.keySet();
   }

   /**
    * Puts a bean instance under the bean key in the map
    * 
    * @param bean The bean
    * @param instance the instance
    * 
    * @see org.jboss.webbeans.context.api.BeanStore#put(Bean, Object)
    */
   public <T> void put(Contextual<? extends T> bean, T instance)
   {
      delegate.put(bean, instance);
      log.trace("Stored instance " + instance + " for bean " + bean + " in bean map");
   }

   @Override
   public String toString()
   {
      return "holding " + delegate.size() + " instances";
   }

}