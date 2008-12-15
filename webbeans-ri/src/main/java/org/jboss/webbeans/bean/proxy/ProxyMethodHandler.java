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

package org.jboss.webbeans.bean.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Context;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.util.Reflections;

/**
 * A Javassist MethodHandler that delegates method calls to a proxied bean. If
 * the transient bean has become null, it is looked up from the manager bean
 * list before the invocation.
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.webbeans.bean.proxy.ProxyPool
 */
public class ProxyMethodHandler implements MethodHandler, Serializable
{
   private static final long serialVersionUID = -5391564935097267888L;
   // The bean
   private transient Bean<?> bean;
   // The bean index in the manager
   private int beanIndex;

   /**
    * Constructor
    * 
    * @param bean The bean to proxy
    * @param beanIndex The index to the bean in the manager bean list
    */
   public ProxyMethodHandler(Bean<?> bean, int beanIndex)
   {
      this.bean = bean;
      this.beanIndex = beanIndex;
   }

   /**
    * The method proxy
    * 
    * Uses reflection to look up the corresponding method on the proxy and
    * executes that method with the same parameters.
    * 
    * @param self A reference to the proxy
    * @param method The method to execute
    * @param process The next method to proceed to
    * @param args The method calling arguments
    */
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      //TODO account for child managers
      if (bean == null)
      {
         bean = CurrentManager.rootManager().getBeans().get(beanIndex);
      }
      Context context = CurrentManager.rootManager().getContext(bean.getScopeType());
      Object proxiedInstance = context.get(bean, true);
      Method proxiedMethod = Reflections.lookupMethod(method, proxiedInstance);
      return proxiedMethod.invoke(proxiedInstance, args);
   }

   /**
    * Gets a string representation
    * 
    * @return The string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      String beanInfo = bean == null ? "null bean" : bean.toString();
      buffer.append("Proxy method handler for " + beanInfo + " with index " + beanIndex);
      return buffer.toString();
   }

}
