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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.webbeans.manager.Bean;

import com.google.common.collect.ForwardingMap;

/**
 * A BeanMap that uses a simple forwarding HashMap as backing map
 * 
 * @author Nicklas Karlsson
 */
public class SimpleBeanMap extends ForwardingMap<Bean<? extends Object>, Object> implements BeanMap
{

   protected Map<Bean<? extends Object>, Object> delegate;

   public SimpleBeanMap()
   {
      delegate = new ConcurrentHashMap<Bean<? extends Object>, Object>();
   }

   @SuppressWarnings("unchecked")
   public <T extends Object> T get(Bean<? extends T> bean)
   {
      return (T) super.get(bean);
   }

   @Override
   public Map<Bean<? extends Object>, Object> delegate()
   {
      return delegate;
   }
   
   @SuppressWarnings("unchecked")
   public <T extends Object> T remove(Bean<? extends T> bean)
   {
      return (T) super.remove(bean);
   }
   
   public void clear() {
      delegate.clear();
   }
   
   public Set<Bean<? extends Object>> keySet() {
      return delegate.keySet();
   }
   
   @SuppressWarnings("unchecked")
   public <T> T put(Bean<? extends T> bean, T instance)
   {
      return (T) delegate.put(bean, instance);
   }

}