/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import static org.jboss.weld.logging.Category.CONTEXT;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_INSTANCE_REMOVED;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXTUAL_IS_NULL;
import static org.jboss.weld.logging.messages.ContextMessage.CONTEXT_CLEARED;
import static org.jboss.weld.logging.messages.ContextMessage.NO_BEAN_STORE_AVAILABLE;

import java.lang.annotation.Annotation;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.Container;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.ContextualInstance;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.slf4j.cal10n.LocLogger;

/**
 * Base for the Context implementations. Delegates calls to the abstract
 * getBeanStorage and getActive to allow for different implementations (storage
 * types and ThreadLocal vs. shared)
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 * @see org.jboss.weld.contexts.SharedContext
 * @see org.jboss.weld.context.BasicContext
 */
public abstract class AbstractMapContext extends AbstractContext
{
   private static final LocLogger log = loggerFactory().getLogger(CONTEXT);

   private static ReentrantLock creationLock = new ReentrantLock();
   
   /**
    * Constructor
    * 
    * @param scopeType The scope type
    */
   public AbstractMapContext(Class<? extends Annotation> scopeType)
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
    * @see javax.enterprise.context.spi.Context#get(BaseBean, boolean)
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      if (getBeanStore() == null)
      {
         return null;
      }
      if (contextual == null)
      {
         throw new IllegalArgumentException(CONTEXTUAL_IS_NULL);
      }
      String id = getId(contextual);
      ContextualInstance<T> beanInstance = getBeanStore().get(id);
      if (beanInstance != null)
      {
         return beanInstance.getInstance();
      }
      else if (creationalContext != null)
      {
         boolean needCreationLock = isCreationLockRequired();
         try
         {
            if (needCreationLock)
            {
               creationLock.lock();
               beanInstance = getBeanStore().get(id);
               if (beanInstance != null)
               {
                  return beanInstance.getInstance();
               }
            }
            T instance = contextual.create(creationalContext);
            if (instance != null)
            {
               beanInstance = new SerializableContextualInstanceImpl<Contextual<T>, T>(contextual, instance, creationalContext);
               getBeanStore().put(id, beanInstance);
            }
            return instance;
         }
         finally
         {
            if (needCreationLock)
            {
               creationLock.unlock();
            }
         }
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
   
   private <T> void destroy(String id)
   {
      if (getBeanStore() == null)
      {
         throw new IllegalStateException(NO_BEAN_STORE_AVAILABLE, this);
      }
      ContextualInstance<T> beanInstance = getBeanStore().get(id);
      beanInstance.getContextual().destroy(beanInstance.getInstance(), beanInstance.getCreationalContext());
      log.trace(CONTEXTUAL_INSTANCE_REMOVED, id, this);
   }
   

   /**
    * Destroys the context
    */
   public void destroy()
   {
      log.trace(CONTEXT_CLEARED, this);
      if (getBeanStore() == null)
      {
         throw new IllegalStateException(NO_BEAN_STORE_AVAILABLE, this);
      }
      for (String id : getBeanStore().getContextualIds())
      {
         destroy(id);
      }
      getBeanStore().clear();
   }

   /**
    * A method that returns the actual bean store implementation
    * 
    * @return The bean store
    */
   protected abstract BeanStore getBeanStore();
   
   /**
    * If Context need to inhibit concurrent instance creation then true, else false.  
    * @return need lock
    */
   protected abstract boolean isCreationLockRequired();

   
   @Override
   public void cleanup()
   {
      super.cleanup();
      if (getBeanStore() != null)
      {
         getBeanStore().clear();
      }
   }
   
   protected static <T> Contextual<T> getContextual(String id)
   {
      return Container.instance().services().get(ContextualStore.class).<Contextual<T>, T>getContextual(id);
   }
   
   protected static String getId(Contextual<?> contextual)
   {
      return Container.instance().services().get(ContextualStore.class).putIfAbsent(contextual);
   }
   
}
