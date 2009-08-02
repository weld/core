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
package org.jboss.webbeans.resolution;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.collections.ConcurrentCache;

/**
 * Implementation of name based bean resolution
 * 
 * @author Pete Muir
 */
public class NameBasedResolver
{
   // The resolved names
   private ConcurrentCache<String, Set<Bean<?>>> resolvedNames;
   
   // The beans to search
   private final Iterable<? extends Bean<?>> allBeans;
   
   // The Web Beans manager
   private final BeanManagerImpl manager;

   /**
    * Constructor
    * 
    */
   public NameBasedResolver(BeanManagerImpl manager, Iterable<? extends Bean<?>> allBeans)
   {
      this.manager = manager;
      this.allBeans = allBeans;
      this.resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
   }

   /**
    * Reset all cached injection points. You must reset all cached injection
    * points when you add a bean to the manager
    */
   public void clear()
   {
      this.resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
   }

   /**
    * Get the possible beans for the given name
    * 
    * @param name The name to match
    * @return The set of matching beans
    */
   public Set<Bean<?>> resolve(final String name)
   {
      return resolvedNames.putIfAbsent(name, new Callable<Set<Bean<?>>>()
      {

         public Set<Bean<? extends Object>> call() throws Exception
         {
            Set<Bean<?>> matchedBeans = new HashSet<Bean<?>>();
            for (Bean<?> bean : allBeans)
            {
               if ((bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
               {
                  matchedBeans.add(bean);
               }
            }
            return Beans.retainEnabledPolicies(matchedBeans, manager.getEnabledPolicyClasses(), manager.getEnabledPolicyStereotypes());
         }

      });
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Resolver\n");
      buffer.append("Resolved names points: " + resolvedNames.size() + "\n");
      return buffer.toString();
   }

   protected BeanManagerImpl getManager()
   {
      return manager;
   }

}
