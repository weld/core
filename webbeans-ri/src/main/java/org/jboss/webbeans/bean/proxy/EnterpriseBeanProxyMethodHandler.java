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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

import javax.webbeans.Dependent;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

/**
 * Method handler for enterprise bean client proxies
 * 
 * @author Nicklas Karlsson
 * @author Pete Muir
 * 
 */
public class EnterpriseBeanProxyMethodHandler implements MethodHandler
{
   // The log provider
   private static final transient LogProvider log = Logging.getLogProvider(EnterpriseBeanProxyMethodHandler.class);

   // The container provided proxy that implements all interfaces
   private final Map<Class<?>, Object> proxiedInstances;
   private final Map<Class<?>, String> jndiNames;
   private boolean destroyed;
   private boolean canCallRemoveMethods;
   private final List<Method> removeMethods;

   /**
    * Constructor
    * 
    * @param removeMethods
    * 
    * @param proxy The generic proxy
    */
   public EnterpriseBeanProxyMethodHandler(EnterpriseBean<?> bean, Iterable<Method> removeMethods)
   {
      this.proxiedInstances = new HashMap<Class<?>, Object>();
      this.jndiNames = bean.getEjbDescriptor().getLocalBusinessInterfacesJndiNames();
      this.canCallRemoveMethods = bean.canCallRemoveMethods();
      this.removeMethods = bean.getEjbDescriptor().getRemoveMethods();
      this.destroyed = false;
      log.trace("Created enterprise bean proxy method handler for " + bean);
   }

   /**
    * Lookups the EJB in the container and executes the method on it
    * 
    * @param self the proxy instance.
    * @param method the overridden method declared in the super class or
    *           interface.
    * @param proceed the forwarder method for invoking the overridden method. It
    *           is null if the overridden method is abstract or declared in the
    *           interface.
    * @param args an array of objects containing the values of the arguments
    *           passed in the method invocation on the proxy instance. If a
    *           parameter type is a primitive type, the type of the array
    *           element is a wrapper class.
    * @return the resulting value of the method invocation.
    * 
    * @throws Throwable if the method invocation fails.
    */
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      if ("isDestroyed".equals(method.getName()))
      {
         return destroyed;
      }
      Class<?> businessInterface = method.getDeclaringClass();
      Object proxiedInstance = proxiedInstances.get(businessInterface);
      if (proxiedInstance == null)
      {
         String jndiName = jndiNames.get(businessInterface);
         if (jndiName == null)
         {
            throw new IllegalStateException("Unable to establish jndi name to use to lookup EJB");
         }
         proxiedInstance = CurrentManager.rootManager().getNaming().lookup(jndiName, businessInterface);
         proxiedInstances.put(businessInterface, proxiedInstance);
      }
      Method proxiedMethod = Reflections.lookupMethod(method, proxiedInstance);
      if (removeMethods.contains(proxiedMethod))
      {
         if (canCallRemoveMethods)
         {
            destroyed = true;
         }
         else
         {
            throw new UnsupportedOperationException("Remove method can't be called directly on non-dependent scoped Enterprise Beans");
         }
      }
      Object returnValue = Reflections.invokeAndWrap(proxiedMethod, proxiedInstance, args);
      log.trace("Executed " + method + " on " + proxiedInstance + " with parameters " + args + " and got return value " + returnValue);
      return returnValue;
   }
}
