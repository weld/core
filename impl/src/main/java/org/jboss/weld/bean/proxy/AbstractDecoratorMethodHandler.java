/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
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

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.InjectionPoint;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.ParameterInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.jlr.MethodSignatureImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * {@link MethodHandler} for Abstract decorators.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class AbstractDecoratorMethodHandler implements MethodHandler
{

   private WeldClass<?> delegateClass;

   private InjectionPoint injectionPoint;

   private Object delegate;

   public AbstractDecoratorMethodHandler(WeldClass<?> delegateClass, InjectionPoint injectionPoint, Object injectedDelegate)
   {
      this.delegateClass = delegateClass;
      this.injectionPoint = injectionPoint;
      this.delegate = injectedDelegate;
   }

   public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable
   {
      // intercept injection of delegate if not set already
      // assumes that injection happens on a single thread
      // TODO: replace this way of initializing field-injected delegates (move out) - MBG
      if (delegate == null)
      {
         if (injectionPoint instanceof FieldInjectionPoint)
         {
            this.delegate = ((FieldInjectionPoint) injectionPoint).get(self);
         }
         else
         if (injectionPoint.getMember() instanceof Method && injectionPoint instanceof ParameterInjectionPoint<?, ?>)
         {
            if (thisMethod.equals(injectionPoint.getMember()))
            {
               int position = ((ParameterInjectionPoint<?, ?>) injectionPoint).getPosition();
               delegate = args[position];
            }
         }
      }
      // if method is abstract, invoke the corresponding method on the delegate
      if (Reflections.isAbstract(thisMethod))
      {
         Method method = ((AnnotatedMethod<?>) delegateClass.getWeldMethod(new MethodSignatureImpl(thisMethod))).getJavaMember();
         return Reflections.invoke(method, delegate, args);
      }

      return proceed.invoke(self, args);

   }
}
