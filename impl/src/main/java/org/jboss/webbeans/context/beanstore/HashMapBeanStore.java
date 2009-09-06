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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.spi.Contextual;

import org.jboss.webbeans.context.api.ContexutalInstance;
import org.jboss.webbeans.context.api.helpers.AbstractMapBackedBeanStore;

/**
 * A BeanStore that uses a HashMap as backing storage
 * 
 * @author Nicklas Karlsson
 */
public class HashMapBeanStore extends AbstractMapBackedBeanStore implements Serializable
{
   
   private static final long serialVersionUID = 4770689245633688471L;
   
   // The backing map
   protected Map<Contextual<? extends Object>, ContexutalInstance<? extends Object>> delegate;

   /**
    * Constructor
    */
   public HashMapBeanStore()
   {
      delegate = new HashMap<Contextual<? extends Object>, ContexutalInstance<? extends Object>>();
   }

   /**
    * Gets the delegate for the store
    * 
    * @return The delegate
    */
   @Override
   public Map<Contextual<? extends Object>, ContexutalInstance<? extends Object>> delegate()
   {
      return delegate;
   }

}