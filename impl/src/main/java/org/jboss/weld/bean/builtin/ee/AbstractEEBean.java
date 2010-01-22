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
package org.jboss.weld.bean.builtin.ee;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.CallableMethodHandler;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.Proxies.TypeInfo;

public abstract class AbstractEEBean<T> extends AbstractBuiltInBean<T>
{
   
   private final T proxy;
   private final Class<T> type;
   private final Set<Type> types;

   protected AbstractEEBean(Class<T> type, Callable<T> callable, BeanManagerImpl beanManager)
   {
      super(type.getSimpleName(), beanManager);
      this.type = type;
      this.types = new HashSet<Type>();
      this.types.add(Object.class);
      this.types.add(type);
      try
      {
         this.proxy = Proxies.<T>createProxy(new CallableMethodHandler(callable), TypeInfo.of(getTypes()).add(Serializable.class));
      }
      catch (InstantiationException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_FAILED, e, this);
      }
      catch (IllegalAccessException e)
      {
         throw new DefinitionException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
      }
   }

   public T create(CreationalContext<T> creationalContext)
   {
      return proxy;
   }
   
   public void destroy(T instance, CreationalContext<T> creationalContext) 
   {
      // no-op
   }
   
   @Override
   public Class<T> getType()
   {
      return type;
   }
   
   public Set<Type> getTypes()
   {
      return types;
   }

}
