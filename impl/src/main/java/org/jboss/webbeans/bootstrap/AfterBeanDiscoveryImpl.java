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
package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.webbeans.BeanManagerImpl;

public class AfterBeanDiscoveryImpl implements AfterBeanDiscovery
{
   
   private final List<Throwable> definitionErrors = new ArrayList<Throwable>();
   private final BeanManagerImpl beanManager;
   
   public AfterBeanDiscoveryImpl(BeanManagerImpl beanManager)
   {
      this.beanManager = beanManager;
   }

   public void addDefinitionError(Throwable t)
   {
      definitionErrors.add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return Collections.unmodifiableList(definitionErrors);
   }
   
   public void addBean(Bean<?> bean)
   {
      beanManager.addBean(bean);
   }

   public void addContext(Context context)
   {
      beanManager.addContext(context);
   }

   public void addObserverMethod(ObserverMethod<?, ?> observerMethod)
   {
      beanManager.addObserver(observerMethod);
   }

}
