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

package org.jboss.webbeans.context;

import java.util.concurrent.atomic.AtomicInteger;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.Dependent;
import javax.webbeans.manager.Contextual;

import org.jboss.webbeans.bean.AbstractClassBean;

/**
 * The dependent context
 * 
 * @author Nicklas Karlsson
 */
public class DependentContext extends AbstractContext
{

   public static DependentContext INSTANCE = new DependentContext();

   private ThreadLocal<AtomicInteger> reentrantActiveCount;
   private ThreadLocal<Object> currentInjectionInstance;


   /**
    * Constructor
    */
   public DependentContext()
   {
      super(Dependent.class);
      super.setActive(false);
      this.reentrantActiveCount = new ThreadLocal<AtomicInteger>()
      {
         @Override
         protected AtomicInteger initialValue()
         {
            return new AtomicInteger(0);
         }
      };
      this.currentInjectionInstance = new ThreadLocal<Object>();
   }

   /**
    * Overridden method always creating a new instance
    * 
    * @param bean The bean to create
    * @param create Should a new one be created
    */
   public <T> T get(Contextual<T> bean, boolean create)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      T instance = create == false ? null : bean.create();
      if (bean instanceof AbstractClassBean && (currentInjectionInstance.get() != null))
      {
         DependentInstancesStore dependentInstancesStore = ((AbstractClassBean<?>) bean).getDependentInstancesStore();
         dependentInstancesStore.addDependentInstance(currentInjectionInstance.get(), ContextualInstance.of(bean, instance));
      }
      return instance;
   }

   @Override
   public String toString()
   {
      String active = isActive() ? "Active " : "Inactive ";
      return active + "dependent context";
   }

   @Override
   public void setActive(boolean active)
   {
      if (active)
      {
         if (reentrantActiveCount.get().incrementAndGet() == 1)
         {
            super.setActive(true);
         }
      }
      else
      {
         if (reentrantActiveCount.get().decrementAndGet() == 0)
         {
            super.setActive(false);
         }
      }
   }
   
   public void setCurrentInjectionInstance(Object currentInjectionInstance)
   {
      if (this.currentInjectionInstance.get() == null)
      {
         this.currentInjectionInstance.set(currentInjectionInstance);
      }
   }

   public void clearCurrentInjectionInstance(Object instance)
   {
      if (this.currentInjectionInstance.get() == instance)
      {
         this.currentInjectionInstance.set(null);
      }
   }
   
}
