/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_BEAN_ACCESS_FAILED;
import static org.jboss.weld.logging.messages.BeanMessage.PROXY_INSTANTIATION_FAILED;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Proxies;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * @author Marius Bogoevici
 */
public class DecorationHelper<T>
{
   private static ThreadLocal<Stack<DecorationHelper<?>>> helperStackHolder = new ThreadLocal<Stack<DecorationHelper<?>>>()
   {
      @Override protected Stack<DecorationHelper<?>> initialValue()
      {
         return new Stack<DecorationHelper<?>>();
      }
   };

   private Class<T> proxyClassForDecorator;

   private T originalInstance;

   private T previousDelegate;

   private int counter;

   private BeanManagerImpl beanManager;

   List<Decorator<?>> decorators;

   public DecorationHelper(T originalInstance, Class<T> proxyClassForDecorator, BeanManagerImpl beanManager, List<Decorator<?>> decorators)
   {
      this.originalInstance = originalInstance;
      this.beanManager = beanManager;
      this.decorators = new LinkedList<Decorator<?>>(decorators);
      this.proxyClassForDecorator = proxyClassForDecorator;
      counter = 0;
   }

   public static Stack<DecorationHelper<?>> getHelperStack()
   {
      return helperStackHolder.get();
   }

   public DecoratorProxyMethodHandler createMethodHandler(InjectionPoint injectionPoint, CreationalContext<?> creationalContext, Decorator<Object> decorator)
   {
      Object decoratorInstance = beanManager.getReference(injectionPoint, decorator, creationalContext);
      SerializableContextualInstanceImpl<Decorator<Object>, Object> serializableContextualInstance = new SerializableContextualInstanceImpl<Decorator<Object>, Object>(decorator, decoratorInstance, null);
      return new DecoratorProxyMethodHandler(serializableContextualInstance, previousDelegate);
   }

   public T getNextDelegate(InjectionPoint injectionPoint, CreationalContext<?> creationalContext)
   {
      if (counter == decorators.size())
      {
         previousDelegate = originalInstance;
         return originalInstance;
      }
      else
      {
         try
         {
            T proxy = SecureReflections.newInstance(proxyClassForDecorator);
            Proxies.attachMethodHandler(proxy, createMethodHandler(injectionPoint, creationalContext, (Decorator<Object>) decorators.get(counter++)));
            previousDelegate = proxy;
            return proxy;
         }
         catch (InstantiationException e)
         {
            throw new WeldException(PROXY_INSTANTIATION_FAILED, e, this);
         }
         catch (IllegalAccessException e)
         {
            throw new WeldException(PROXY_INSTANTIATION_BEAN_ACCESS_FAILED, e, this);
         }

      }
   }

}
