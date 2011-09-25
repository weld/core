/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.io.Serializable;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

import javax.enterprise.inject.spi.Bean;

/**
 * A simple {@link BeanInstance} which always maintains a specific bean instance
 * that is being proxied.
 *
 * @author David Allen
 */
public class TargetBeanInstance extends AbstractBeanInstance implements Serializable
{
   private static final long serialVersionUID = 1099995238604086450L;
   private final Object      instance;
   private final Class<?>    instanceType;
   private MethodHandler     interceptorsHandler;

   public TargetBeanInstance(Bean<?> bean, Object instance) {
      this.instance = instance;
      this.instanceType = computeInstanceType(bean);
   }

   public TargetBeanInstance(Object instance) {
      this.instance = instance;
      this.instanceType = (Class<?>) instance.getClass();
   }

   /**
    * Copy constructor
    *
    * @param otherBeanInstance other bean instance to copy
    */
    public TargetBeanInstance(TargetBeanInstance otherBeanInstance) {
        this.instance = otherBeanInstance.instance;
        this.instanceType = otherBeanInstance.instanceType;
        this.interceptorsHandler = otherBeanInstance.interceptorsHandler;
    }

    public Object getInstance() {
        return instance;
    }

    public Class<?> getInstanceType()  {
        return instanceType;
    }

    /**
     * @return the interceptorsHandler
     */
    public MethodHandler getInterceptorsHandler() {
        return interceptorsHandler;
    }

    /**
     * @param interceptorsHandler the interceptorsHandler to set
     */
     public void setInterceptorsHandler(MethodHandler interceptorsHandler) {
        this.interceptorsHandler = interceptorsHandler;
     }

    @Override
   public Object invoke(Object instance, Method method, Object... arguments) throws Throwable {
      if (interceptorsHandler != null) {
         log.trace("Invoking interceptor chain for method " + method.toGenericString() + " on " + instance);
         if (method.getDeclaringClass().isInterface()) {
            return interceptorsHandler.invoke(instance, method, null, arguments);
         }
         else {
            return interceptorsHandler.invoke(instance, method, method, arguments);
         }
      }
      else {
         log.trace("Invoking method " + method.toGenericString() + " directly on " + instance);
         return super.invoke(instance, method, arguments);
      }
   }

}
