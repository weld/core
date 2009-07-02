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
package org.jboss.webbeans;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.bootstrap.api.Service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Application wide bean identifier service which allows a serializable
 * reference to a bean to be obtained, and the bean object to be got for a given
 * id. Note that this allows a Bean object to be loaded regardless of the bean's
 * accessiblity from the current module, and should not be abused as a way to
 * ignore accessibility rules enforced during resolution.
 * 
 * @author Pete Muir
 * 
 */
public class BeanIdStore implements Service
{
   
   private static class BeanHolder<T>
   {
      public static <T>  BeanHolder<T> of(Bean<T> bean, BeanManagerImpl manager)
      {
         return new BeanHolder<T>(bean, manager);
      }
      
      private final Bean<T> bean;
      private final BeanManagerImpl manager;
      
      public BeanHolder(Bean<T> bean, BeanManagerImpl manager)
      {
         this.bean = bean;
         this.manager = manager;
      }

      public Bean<T> getBean()
      {
         return bean;
      }
      
      public BeanManagerImpl getManager()
      {
         return manager;
      }
      
      @Override
      public int hashCode()
      {
         return bean.hashCode();
      }
      
      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof BeanHolder)
         {
            return (this.bean.equals(((BeanHolder<?>) obj).getBean()));
         }
         else
         {
            return false;
         }
      }
   }
   
   private final BiMap<Integer, BeanHolder<?>> beans;
   private final AtomicInteger idGenerator;
   
   public BeanIdStore()
   {
      this.idGenerator = new AtomicInteger(0);
      this.beans = HashBiMap.create();
   }
   
   public <T> Bean<T> get(Integer id)
   {
      return (Bean<T>) beans.get(id).getBean();
   }
   
   public boolean contains(Integer id)
   {
      return beans.containsKey(id);
   }
   
   public Integer get(Bean<?> bean, BeanManagerImpl manager)
   {
      if (beans.inverse().containsKey(bean))
      {
         return beans.inverse().get(bean);
      }
      else
      {
         Integer id = idGenerator.incrementAndGet();
         beans.put(id, BeanHolder.of(bean, manager));
         return id;
      }
   }
}
