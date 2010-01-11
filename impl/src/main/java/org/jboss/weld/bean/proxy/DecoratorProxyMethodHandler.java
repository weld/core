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

import static org.jboss.weld.logging.messages.BeanMessage.UNEXPECTED_UNWRAPPED_CUSTOM_DECORATOR;

import java.lang.reflect.Method;
import java.util.List;

import javax.enterprise.inject.spi.Decorator;

import org.jboss.interceptor.util.proxy.TargetInstanceProxyMethodHandler;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.exceptions.ForbiddenStateException;
import org.jboss.weld.introspector.MethodSignature;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * Method handler for decorated beans
 * 
 * @author Pete Muir
 * 
 */
public class DecoratorProxyMethodHandler extends TargetInstanceProxyMethodHandler
{
   private static final long serialVersionUID = 4577632640130385060L;

   private final List<SerializableContextualInstance<Decorator<Object>, Object>> decoratorInstances;

   /**
    * Constructor
    * 
    * @param removeMethods
    * 
    * @param proxy The generic proxy
    */
   public DecoratorProxyMethodHandler(List<SerializableContextualInstance<Decorator<Object>, Object>> decoratorInstances, Object instance)
   {
      super (instance, instance.getClass());
      this.decoratorInstances = decoratorInstances;
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
   @Override
   protected Object doInvoke(Object self, Method method, Method proceed, Object[] args) throws Throwable
   {
      MethodSignature methodSignature = new MethodSignatureImpl(method);
      for (SerializableContextualInstance<Decorator<Object>, Object> beanInstance : decoratorInstances)
      {
         if (beanInstance.getContextual().get() instanceof WeldDecorator<?>)
         {
            WeldDecorator<?> decorator = (WeldDecorator<?>) beanInstance.getContextual().get();
            if (decorator.getDecoratedMethodSignatures().contains(methodSignature))
            {
               WeldMethod<?, ?> decoratorMethod = decorator.getWeldAnnotated().getWeldMethod(methodSignature);
               if (decoratorMethod != null)
               {
                  return decoratorMethod.invokeOnInstance(beanInstance.getInstance(), args);
               }
            }
         }
         else
         {
            throw new ForbiddenStateException(UNEXPECTED_UNWRAPPED_CUSTOM_DECORATOR, beanInstance.getContextual().get());
         }
      }
      return SecureReflections.invoke(getTargetInstance(), method, args);
   }
}
