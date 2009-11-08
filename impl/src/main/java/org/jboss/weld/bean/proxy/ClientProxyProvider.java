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
package org.jboss.weld.bean.proxy;

import static org.jboss.weld.logging.messages.BeanMessage.BEAN_ID_CREATION_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import java.io.Serializable;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.WeldException;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;
import org.jboss.weld.util.collections.ConcurrentCache;

/**
 * A proxy pool for holding scope adaptors (client proxies)
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.weld.bean.proxy.ProxyMethodHandler
 */
public class ClientProxyProvider
{
   private static final long serialVersionUID = 9029999149357529341L;

   /**
    * A container/cache for previously created proxies
    * 
    * @author Nicklas Karlsson
    */
   private final ConcurrentCache<Bean<? extends Object>, Object> pool;

   /**
    * Constructor
    */
   public ClientProxyProvider()
   {
      this.pool = new ConcurrentCache<Bean<? extends Object>, Object>();
   }

   /**
    * Creates a Javassist scope adaptor (client proxy) for a bean
    * 
    * Creates a Javassist proxy factory. Gets the type info. Sets the interfaces
    * and superclass to the factory. Hooks in the MethodHandler and creates the
    * proxy.
    * 
    * @param bean The bean to proxy
    * @param beanIndex The index to the bean in the manager bean list
    * @return A Javassist proxy
    * @throws InstantiationException When the proxy couldn't be created
    * @throws IllegalAccessException When the proxy couldn't be created
    */
   private static <T> T createClientProxy(Bean<T> bean, BeanManagerImpl manager, String id) throws RuntimeException
   {
      try
      {
         return Proxies.<T>createProxy(new ClientProxyMethodHandler(bean, manager, id), TypeInfo.of(bean.getTypes()).add(Serializable.class));
      }
      catch (InstantiationException e)
      {
         throw new WeldException(PROXY_INSTANTIATION_FAILED, e, bean);
      }
      catch (IllegalAccessException e)
      {
         throw new WeldException(BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, bean);
      }
   }

   /**
    * Gets a client proxy for a bean
    * 
    * Looks for a proxy in the pool. If not found, one is created and added to
    * the pool if the create argument is true.
    * 
    * @param bean The bean to get a proxy to
    * @return the client proxy for the bean
    */
   public <T> T getClientProxy(final BeanManagerImpl manager, final Bean<T> bean)
   {
      T instance = pool.putIfAbsent(bean, new Callable<T>()
      {

         public T call() throws Exception
         {
            String id = Container.instance().deploymentServices().get(ContextualStore.class).putIfAbsent(bean);
            if (id == null)
            {
               throw new DefinitionException(BEAN_ID_CREATION_FAILED, bean);
            }
            return createClientProxy(bean, manager, id);
         }

      });
      return instance;
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      return "Proxy pool with " + pool.size() + " proxies";
   }
   
   public void clear()
   {
      this.pool.clear();
   }

}
