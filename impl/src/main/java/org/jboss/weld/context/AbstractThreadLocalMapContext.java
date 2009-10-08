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
package org.jboss.weld.context;

import java.lang.annotation.Annotation;

import org.jboss.weld.context.api.BeanStore;

/**
 * Abstract base class for representing contexts with thread local bean storage
 * 
 * @author Pete Muir
 */
public abstract class AbstractThreadLocalMapContext extends AbstractMapContext
{
   private final ThreadLocal<BeanStore> beanStore;

   public AbstractThreadLocalMapContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
      this.beanStore = new ThreadLocal<BeanStore>();
   }

   /**
    * Gets the bean store
    * 
    * @returns The bean store
    */
   @Override
   public BeanStore getBeanStore()
   {
      return beanStore.get();
   }

   /**
    * Sets the bean store
    * 
    * @param beanStore The bean store
    */
   public void setBeanStore(BeanStore beanStore)
   {
      this.beanStore.set(beanStore);
   }
   
   @Override
   protected boolean isCreationLockRequired()
   {
      return true;
   }
   
   @Override
   public void cleanup()
   {
      super.cleanup();
      beanStore.remove();
   }
   
}