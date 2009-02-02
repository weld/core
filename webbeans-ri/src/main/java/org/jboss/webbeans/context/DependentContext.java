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

import javax.context.ContextNotActiveException;
import javax.context.Contextual;
import javax.context.CreationalContext;
import javax.context.Dependent;

/**
 * The dependent context
 * 
 * @author Nicklas Karlsson
 */
public class DependentContext extends AbstractContext
{
   public static DependentContext INSTANCE = new DependentContext();

   private ThreadLocal<AtomicInteger> reentrantActiveCount;
   // Key to collect instances under in DependentInstacesStore
   private ThreadLocal<Object> dependentsKey;

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
      this.dependentsKey = new ThreadLocal<Object>();
   }

   /**
    * Overridden method always creating a new instance
    * 
    * @param contextual The bean to create
    * @param create Should a new one be created
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      if (creationalContext != null)
      {
         T instance = contextual.create(creationalContext);
         if (dependentsKey.get() != null)
         {
            DependentInstancesStore.instance().addDependentInstance(dependentsKey.get(), ContextualInstance.of(contextual, instance));
         }
         return instance;
      }
      else
      {
         return null;
      }
   }
   
   public <T> T get(Contextual<T> contextual)
   {
      return get(contextual, null);
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

   /**
    * Sets the current injection instance. If there is already an instance
    * registered, nothing is done.
    * 
    * @param dependentsKey The current injection instance to register
    *           dependent objects under
    */
   public void startCollecting(Object dependentsKey)
   {
      if (this.dependentsKey.get() == null)
      {
         this.dependentsKey.set(dependentsKey);
      }
   }

   /**
    * Clears the current injection instance. Can only be done by passing in the instance
    * of the current instance.
    * 
    * @param dependentsKey The instance to free
    */
   public void stopCollecting(Object dependentsKey)
   {
      if (this.dependentsKey.get() == dependentsKey)
      {
         this.dependentsKey.set(null);
      }
   }

}
