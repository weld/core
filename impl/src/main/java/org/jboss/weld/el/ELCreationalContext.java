/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.el;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.context.ForwardingWeldCreationalContext;
import org.jboss.weld.context.WeldCreationalContext;

abstract class ELCreationalContext<T> extends ForwardingWeldCreationalContext<T>
{
   
   public static <X> ELCreationalContext<X> of(final WeldCreationalContext<X> creationalContext)
   {
      return new ELCreationalContext<X>()
      {
         
         @Override
         protected WeldCreationalContext<X> delegate()
         {
            return creationalContext;
         }
         
      };
   }

   private final Map<String, Object> dependentInstances;
   
   public ELCreationalContext()
   {
      this.dependentInstances = new HashMap<String, Object>();
   }
   
   public Object putIfAbsent(Bean<?> bean, Callable<Object> value) throws Exception
   {
      if (bean.getScope().equals(Dependent.class))
      {
         if (dependentInstances.containsKey(bean.getName()))
         {
            return dependentInstances.get(bean.getName());
         }
         else
         {
            Object instance = value.call();
            dependentInstances.put(bean.getName(), instance);
            return instance;
         }
      }
      else
      {
         return value.call();
      }
   }
   
}