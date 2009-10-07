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

package org.jboss.webbeans.bean.interceptor;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.proxy.DirectClassInterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.webbeans.BeanManagerImpl;

/**
 * @author Marius Bogoevici
*/
public class InterceptorInterceptionHandlerFactory implements InterceptionHandlerFactory<Interceptor<?>>
{
   private final CreationalContext creationalContext;
   private BeanManagerImpl manager;

   public InterceptorInterceptionHandlerFactory(CreationalContext creationalContext, BeanManagerImpl manager)
   {
      this.creationalContext = creationalContext;
      this.manager = manager;
   }

   public BeanManagerImpl getManager()
   {
      return manager;
   }

   public InterceptionHandler createFor(final Interceptor interceptor)
   {
      Object instance = getManager().getReference(interceptor, creationalContext);
      return new DirectClassInterceptionHandler(instance, interceptor.getBeanClass());
   }

}
