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

import javassist.util.proxy.MethodHandler;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

/**
 * Method handler for enterprise bean client proxies
 * 
 * @author Nicklas Karlsson
 *
 */
public class EnterpriseBeanProxyMethodHandler implements MethodHandler
{
   // The log provider
   private LogProvider log = Logging.getLogProvider(EnterpriseBeanProxyMethodHandler.class);
   // The container provided proxy that implements all interfaces
   private Object proxy;

   /**
    * Constructor
    * 
    * @param proxy The generic proxy
    */
   public EnterpriseBeanProxyMethodHandler(Object proxy)
   {
      this.proxy = proxy;
      log.trace("Created enterprise bean proxy method handler for " + proxy);
   }

   /**
    * The method proxy
    * 
    * Executes the corresponding method on the proxy
    * 
    * @param self A reference to the proxy
    * @param method The method to execute
    * @param process The next method to proceed to
    * @param args The method calling arguments
    */
   //@Override
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      Method proxiedMethod = Reflections.lookupMethod(method, proxy);
      Object returnValue = Reflections.invokeAndWrap(proxiedMethod, proxy, args);
      log.trace("Executed " + method + " on " + proxy + " with parameters " + args + " and got return value " + returnValue);
      return returnValue;
   }

}
