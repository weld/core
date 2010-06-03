/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.injection;

import java.util.List;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.Bean;

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
}
