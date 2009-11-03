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

import static org.jboss.weld.messages.BeanMessage.CALL_PROXIED_METHOD;
import static org.jboss.weld.util.log.Category.BEAN;
import static org.jboss.weld.util.log.LoggerFactory.loggerFactory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.context.WeldCreationalContext;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.Reflections;
import org.slf4j.cal10n.LocLogger;

/**
 * A Javassist MethodHandler that delegates method calls to a proxied bean. If
 * the transient bean has become null, it is looked up from the manager bean
 * list before the invocation.
 * 
 * @author Nicklas Karlsson
 * 
 * @see org.jboss.weld.bean.proxy.ClientProxyProvider
 */
public class ClientProxyMethodHandler implements MethodHandler, Serializable
{

   private static final long serialVersionUID = -5391564935097267888L;
   // The log provider
   private static final LocLogger log = loggerFactory().getLogger(BEAN);
   // The bean
   private transient Bean<?> bean;
   // The bean index in the manager
   private final String id;

   private final BeanManagerImpl manager;

   private static final ThreadLocal<WeldCreationalContext<?>> currentCreationalContext = new ThreadLocal<WeldCreationalContext<?>>();

   /**
    * Constructor
    * 
    * @param bean The bean to proxy
    * @param beanIndex The index to the bean in the manager bean list
    */
   public ClientProxyMethodHandler(Bean<?> bean, BeanManagerImpl manager, String id)
   {
      this.bean = bean;
      this.id = id;
      this.manager = manager;
      log.trace("Created method handler for bean " + bean + " identified as " + id);
   }

   /**
    * Invokes the method on the correct version of the instance as obtained by a
    * context lookup
    * 
    * @param self the proxy instance.
    * @param proxiedMethod the overridden method declared in the super class or
    *           interface.
    * @param proceed the forwarder method for invoking the overridden method. It
    *           is null if the overridden mehtod is abstract or declared in the
    *           interface.
    * @param args an array of objects containing the values of the arguments
    *           passed in the method invocation on the proxy instance. If a
    *           parameter type is a primitive type, the type of the array
    *           element is a wrapper class.
    * @return the resulting value of the method invocation.
    * 
    * @throws Throwable if the method invocation fails.
    */
   public Object invoke(Object self, Method proxiedMethod, Method proceed, Object[] args) throws Throwable
   {
      if (bean == null)
      {
         bean = Container.instance().deploymentServices().get(ContextualStore.class).<Bean<Object>, Object>getContextual(id);
      }
      Object proxiedInstance = getProxiedInstance(bean);
      if ("touch".equals(proxiedMethod.getName()) && Marker.isMarker(0, proxiedMethod, args))
      {
         // Our "touch" method, which simply ensures the proxy does any object
         // instantiation needed, to avoid the annoying side effect of an object
         // getting lazy created
         return null;
      }
      if (proxiedInstance == null)
      {
         // TODO not sure if this right PLM
         return null;
      }
      if (proxiedMethod.getName().equals("equals")  && proxiedMethod.getParameterTypes().length == 1 && proxiedMethod.getParameterTypes()[0] == Object.class && args[0] == self)
      {
         return true;
      }
      try
      {
         Object returnValue = Reflections.lookupMethod(proxiedMethod, proxiedInstance).invoke(proxiedInstance, args);
         log.trace(CALL_PROXIED_METHOD, proxiedMethod, proxiedInstance, args, returnValue == null ? null : returnValue);
         return returnValue;
      }
      catch (InvocationTargetException e)
      {
         throw e.getCause();
      }
   }

   private <T> T getProxiedInstance(Bean<T> bean)
   {
      WeldCreationalContext<T> creationalContext;
      boolean outer;
      if (currentCreationalContext.get() == null)
      {
         creationalContext = manager.createCreationalContext(bean);
         currentCreationalContext.set(creationalContext);
         outer = true;
      }
      else
      {
         creationalContext = currentCreationalContext.get().getCreationalContext(bean);
         outer = false;
      }
      try
      {
         Context context = manager.getContext(bean.getScope());
         return context.get(bean, creationalContext);
      }
      finally
      {
         if (outer)
         {
            currentCreationalContext.remove();
         }
      }
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
      buffer.append("Proxy method handler for " + beanInfo + " with id " + id);
      return buffer.toString();
   }

}
