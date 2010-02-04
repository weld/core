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

package org.jboss.weld.bean.interceptor;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.model.InterceptorMetadata;
import org.jboss.interceptor.model.metadata.ReflectiveClassReference;
import org.jboss.interceptor.proxy.InterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.weld.context.SerializableContextualInstanceImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

/**
 * @author Marius Bogoevici
*/
public class CdiInterceptorHandlerFactory<T> implements InterceptionHandlerFactory<SerializableContextual<Interceptor<T>, T>>
{
   private final CreationalContext<T> creationalContext;
   private BeanManagerImpl manager;

   public CdiInterceptorHandlerFactory(CreationalContext<T> creationalContext, BeanManagerImpl manager)
   {
      this.creationalContext = creationalContext;
      this.manager = manager;
   }

   public BeanManagerImpl getManager()
   {
      return manager;
   }

   public InterceptionHandler createFor(final SerializableContextual<Interceptor<T>, T> serializableContextual)
   {
      T instance = (T) getManager().getReference(serializableContextual.get(), creationalContext, false);
      InterceptorMetadata interceptionMetadata = getManager().getServices().get(InterceptionMetadataService.class).getInterceptorMetadataRegistry().getInterceptorClassMetadata(ReflectiveClassReference.of(serializableContextual.get().getBeanClass()), false);
      return new CdiInterceptorHandler<T>(new SerializableContextualInstanceImpl<Interceptor<T>, T>(serializableContextual, instance, creationalContext), serializableContextual.get().getBeanClass(), interceptionMetadata);
   }

}
