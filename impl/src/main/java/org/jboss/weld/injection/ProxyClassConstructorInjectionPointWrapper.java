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

package org.jboss.weld.injection;

import javax.decorator.Decorator;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import java.lang.annotation.Annotation;
import java.util.List;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.introspector.WeldConstructor;

/**
 * A wrapper on a {@link ConstructorInjectionPoint}, to be used if a proxy subclass is instantiated instead of the
 * original (e.g. because the original is an abstract {@link Decorator})
 *
 * This is a wrapper class, it is not thread-safe and any instance of this class should be used only for temporarily
 * enhancing the bean instance creation process.
 *
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class ProxyClassConstructorInjectionPointWrapper<T> extends ConstructorInjectionPoint<T>
{
   private ConstructorInjectionPoint<T> originalConstructorInjectionPoint;

   private Object injectedDelegate;

   public ProxyClassConstructorInjectionPointWrapper(Bean<T> declaringBean, WeldConstructor<T> weldConstructor, ConstructorInjectionPoint<T> originalConstructorInjectionPoint)
   {
      super(declaringBean, weldConstructor);
      this.originalConstructorInjectionPoint = originalConstructorInjectionPoint;
   }

   @Override
   public List<ParameterInjectionPoint<?, T>> getWeldParameters()
   {
      return originalConstructorInjectionPoint.getWeldParameters();
   }

   @Override
   protected Object[] getParameterValues(List<ParameterInjectionPoint<?, T>> parameters, Object specialVal, Class<? extends Annotation> specialParam, BeanManagerImpl manager, CreationalContext<?> creationalContext)
   {
      Object[] parameterValues = super.getParameterValues(parameters, specialVal, specialParam, manager, creationalContext);
      if (parameters.size() > 0)
      {
         for (ParameterInjectionPoint<?, T> parameterInjectionPoint: parameters)
         {
            if (parameterInjectionPoint.isDelegate())
            {
               this.injectedDelegate = parameterValues[parameterInjectionPoint.getPosition()];
            }
         }
      }
      return parameterValues;
   }

   /**
    * The delegate injected during the constructed process, if any
    *
    * @return
    */
   public Object getInjectedDelegate()
   {
      return injectedDelegate;
   }
}
