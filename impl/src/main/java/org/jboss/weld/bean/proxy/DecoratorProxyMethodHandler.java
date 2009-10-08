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

import static org.jboss.weld.util.Reflections.ensureAccessible;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import javassist.util.proxy.MethodHandler;

import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.context.SerializableContextualInstance;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;

/**
 * Method handler for decorated beans
 * 
 * @author Pete Muir
 * 
 */
public class DecoratorProxyMethodHandler implements MethodHandler, Serializable
{
   private static final long serialVersionUID = 4577632640130385060L;

   private final List<SerializableContextualInstance<DecoratorImpl<Object>, Object>> decoratorInstances;
   
   private final Object instance;

   /**
    * Constructor
    * 
    * @param removeMethods
    * 
    * @param proxy The generic proxy
    */
   public DecoratorProxyMethodHandler(List<SerializableContextualInstance<DecoratorImpl<Object>, Object>> decoratorInstances, Object instance)
   {
      this.decoratorInstances = decoratorInstances;
      this.instance = instance;
   }
   
   /**
    * 
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
      MethodSignature methodSignature = new MethodSignatureImpl(method);
      for (SerializableContextualInstance<DecoratorImpl<Object>, Object> beanInstance : decoratorInstances)
      {
         WeldMethod<?, ?> decoratorMethod = beanInstance.getContextual().get().getAnnotatedItem().getWBMethod(methodSignature);
         if (decoratorMethod != null)
         {
            return decoratorMethod.invokeOnInstance(beanInstance.getInstance(), args);
         }
      }
      
      return ensureAccessible(method).invoke(instance, args);
   }
   
}
