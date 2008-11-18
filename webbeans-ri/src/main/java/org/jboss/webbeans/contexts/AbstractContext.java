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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.webbeans.ContextNotActiveException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;
import javax.webbeans.manager.Manager;

/**
 * Base for the Context implementations
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 * @see org.jboss.webbeans.contexts.SharedContext
 * @see org.jboss.webbeans.contexts.PrivateContext
 */
public abstract class AbstractContext implements Context
{
   
   private Class<? extends Annotation> scopeType;

   public AbstractContext(Class<? extends Annotation> scopeType)
   {
      this.scopeType = scopeType;
   }

   /**
    * Get the bean if it exists in the contexts.
    * 
    * @param create If true, a new instance of the bean will be created if none
    * exists
    * 
    * @throws ContextNotActiveException if the context is not active
    *  
    */
   public <T> T get(Bean<T> bean, boolean create)
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
    */
   public Class<? extends Annotation> getScopeType()
   {
      return scopeType;
   }

   /**
    * Return true if the context is active
    */
   public boolean isActive()
   {
      return getActive().get();
   }
   
   /** 
    * Set the context active, internal API for WBRI
    * 
    * @param active The new state
    */
   public void setActive(boolean active) {
      getActive().set(active);
   }
   
   // TODO Do we need this
   private <T> void destroy(Manager manager, Bean<T> bean)
   {
      bean.destroy(getBeanMap().get(bean));
   }

   // TODO Do we need this
   public void destroy(Manager manager)
   {
      for (Bean<? extends Object> bean : getBeanMap().keySet())
      {
         destroy(manager, bean);
      }
      getBeanMap().clear();
   }   
   
   protected abstract BeanMap getBeanMap();
   protected abstract AtomicBoolean getActive();

}
