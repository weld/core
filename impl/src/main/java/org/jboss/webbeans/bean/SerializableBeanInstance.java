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
package org.jboss.webbeans.bean;

import java.io.Serializable;

import org.jboss.webbeans.BeanManagerImpl;

public class SerializableBeanInstance<T extends RIBean<I>, I> implements Serializable
{
   
   private static final long serialVersionUID = 7341389081613003687L;
   
   private final BeanManagerImpl manager;
   private final String beanId;
   private final I instance;
   
   public SerializableBeanInstance(T bean, I instance)
   {
      this.manager = bean.getManager();
      this.beanId = bean.getId();
      this.instance = instance;
   }

   @SuppressWarnings("unchecked")
   public T getBean()
   {
      return (T) manager.getRiBeans().get(beanId);
   }
   
   protected BeanManagerImpl getManager()
   {
      return manager;
   }
   
   protected String getBeanId()
   {
      return beanId;
   }
   
   public I getInstance()
   {
      return instance;
   }
   
}