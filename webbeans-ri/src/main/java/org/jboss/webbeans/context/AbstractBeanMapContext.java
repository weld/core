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

import java.lang.annotation.Annotation;

import javax.context.ContextNotActiveException;
import javax.context.Contextual;
import javax.context.CreationalContext;
import javax.inject.manager.Bean;

import org.jboss.webbeans.context.beanmap.BeanMap;

/**
 * Base for the Context implementations. Delegates calls to the abstract
 * getBeanMap and getActive to allow for different implementations (storage
 * types and ThreadLocal vs. shared)
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 * @see org.jboss.webbeans.contexts.SharedContext
 * @see org.jboss.webbeans.context.BasicContext
 */
public abstract class AbstractBeanMapContext extends AbstractContext
{
   /**
    * Constructor
    * 
    * @param scopeType The scope type
    */
   public AbstractBeanMapContext(Class<? extends Annotation> scopeType)
   {
      super(scopeType);
   }

   /**
    * Get the bean if it exists in the contexts.
    * 
    * @param create If true, a new instance of the bean will be created if none
    *           exists
    * @return An instance of the bean
    * @throws ContextNotActiveException if the context is not active
    * 
    * @see javax.context.Context#get(Bean, boolean)
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      T instance = getBeanMap().get(contextual);
      if (instance != null)
      {
         return instance;
      }
      else if (creationalContext != null)
      {
         instance = contextual.create(creationalContext);
         getBeanMap().put(contextual, instance);
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

   /**
    * Destroys a bean
    * 
    * @param <T> The type of the bean
    * @param bean The bean to destroy
    */
   private <T> void destroy(Contextual<T> bean)
   {
      bean.destroy(getBeanMap().get(bean));
   }

   /**
    * Destroys the context
    */
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

}
