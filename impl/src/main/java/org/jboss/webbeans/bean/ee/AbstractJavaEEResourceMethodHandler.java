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
package org.jboss.webbeans.bean.ee;

import java.io.Serializable;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.util.Reflections;

/**
 * Abstract method handler which invokes the a method on a proxied instance
 * 
 * @author Pete Muir
 *
 */
public abstract class AbstractJavaEEResourceMethodHandler implements MethodHandler, Serializable
{
   
   private static final long serialVersionUID = -3171683636451762591L;
   
   private static final Log log = Logging.getLog(AbstractJavaEEResourceMethodHandler.class);

   /**
    * 
    */
   public AbstractJavaEEResourceMethodHandler()
   {
      super();
   }

   /**
    * Lookup the execute the method on the proxied instance obtained from the 
    * container
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
      Object proxiedInstance = getProxiedInstance(null);
      Object returnValue = Reflections.invokeAndWrap(method, proxiedInstance, args);
      log.trace("Executed {0} on {1} with parameters {2} and got return value {3}", method, proxiedInstance, args, returnValue);
      return returnValue;
   }
   
   protected abstract Object getProxiedInstance(Class<?> declaringClass);

}