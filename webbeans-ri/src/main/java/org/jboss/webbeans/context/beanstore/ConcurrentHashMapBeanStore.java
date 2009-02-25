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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.context.Contextual;

import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.AbstractMapBackedBeanStore;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * A BeanStore that uses a HashMap as backing storage
 * 
 * @author Nicklas Karlsson
 */
public class ConcurrentHashMapBeanStore extends AbstractMapBackedBeanStore implements BeanStore
{
   private static LogProvider log = Logging.getLogProvider(ConcurrentHashMapBeanStore.class);
   
   // The backing map
   protected Map<Contextual<? extends Object>, Object> delegate;

   /**
    * Constructor
    */
   public ConcurrentHashMapBeanStore()
   {
      delegate = new ConcurrentHashMap<Contextual<? extends Object>, Object>();
   }

   /**
    * Gets the delegate for the store
    * 
    * @return The delegate
    */
   @Override
   public Map<Contextual<? extends Object>, Object> delegate()
   {
      return delegate;
   }

}