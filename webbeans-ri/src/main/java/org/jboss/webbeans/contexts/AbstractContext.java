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

import java.lang.annotation.Annotation;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Contextual;

/**
 * Base for the Context implementations. Delegates calls to the abstract
 * getBeanMap and getActive to allow for different implementations (storage
 * types and ThreadLocal vs. shared)
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 * @see org.jboss.webbeans.contexts.SharedContext
 * @see org.jboss.webbeans.contexts.BasicContext
 */
public abstract class AbstractContext implements Context
{
   // The scope type
   private Class<? extends Annotation> scopeType;
   private ThreadLocal<Boolean> active;

   /**
    * Constructor
    * 
    * @param scopeType The scope type
    */
   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
      this.active = new ThreadLocal<Boolean>()
      {
         @Override
         protected Boolean initialValue()
         {
            return Boolean.TRUE;
         } 
      };
   }

   /**
    * Get the bean if it exists in the contexts.
    * 
    * @param create If true, a new instance of the bean will be created if none
    *           exists
    * @return An instance of the bean
    * @throws ContextNotActiveException if the context is not active
    * 
    * @see javax.webbeans.manager.Context#get(Bean, boolean)
    */
   public <T> T get(Contextual<T> bean, boolean create)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      T instance = getBeanMap().get(bean);
      if (instance != null)
      {
         return instance;
      }
      if (!create)
      {
         return null;
      }

      // TODO should bean creation be synchronized?
      instance = bean.create();
      getBeanMap().put(bean, instance);
      return instance;
   }

   /**
    * Get the scope the context is for
    * 
    * @return The scope type
    * 
    * @see javax.webbeans.manager.Context#getScopeType()
    */
   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   /**
    * Return true if the context is active
    * 
    * @return The active state
    * 
    * @see javax.webbeans.manager.Context#isActive()
    */
   public boolean isActive()
   {
      return active.get().booleanValue();
   }

   /**
    * Set the context active, internal API for WBRI
    * 
    * @param active The new state
    */
   public void setActive(boolean active)
   {
      this.active.set(Boolean.valueOf(active));
   }

   private <T> void destroy(Contextual<T> bean)
   {
      bean.destroy(getBeanMap().get(bean));
   }

   public void destroy()
   {
      for (Contextual<? extends Object> bean : getBeanMap().keySet())
      {
         destroy(bean);
      }
      getBeanMap().clear();
   }

   /**
    * A method that should return the actual bean map implementation
    * 
    * @return The actual bean map
    */
   protected abstract BeanMap getBeanMap();

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("AbstractContext:\n");
      buffer.append("Scope type: " + getScopeType().toString() + "\n");
      buffer.append("Active: " + getActive().toString() + "\n");
      buffer.append(getBeanMap().toString() + "\n");
      return buffer.toString();
   }

   /**
    * Delegates to a ThreadLocal instance
    */
   protected Boolean getActive()
   {
      return active.get();
   }

}
