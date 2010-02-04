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

import java.lang.reflect.Constructor;

import javax.enterprise.context.spi.CreationalContext;

import org.jboss.interceptor.model.InterceptorMetadata;
import org.jboss.interceptor.model.metadata.ReflectiveClassReference;
import org.jboss.interceptor.proxy.DirectClassInterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandler;
import org.jboss.interceptor.proxy.InterceptionHandlerFactory;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.SecureReflections;

/**
 * @author Marius Bogoevici
*/
public class ClassInterceptionHandlerFactory<T> implements InterceptionHandlerFactory<Class<T>>
{
   private final CreationalContext<T> creationalContext;
   private BeanManagerImpl manager;

   public ClassInterceptionHandlerFactory(CreationalContext<T> creationalContext, BeanManagerImpl manager)
   {
      this.creationalContext = creationalContext;
      this.manager = manager;
   }

   public InterceptionHandler createFor(Class<T> clazz)
   {
      try
      {
         // this is not a managed instance - assume no-argument constructor exists
         Constructor<T> constructor = (Constructor<T>) SecureReflections.getDeclaredConstructor(clazz);
         T interceptorInstance = SecureReflections.ensureAccessible(constructor).newInstance();
         // inject
         manager.createInjectionTarget(manager.createAnnotatedType(clazz)).inject(interceptorInstance, creationalContext);
         InterceptorMetadata interceptionMetadata = manager.getServices().get(InterceptionMetadataService.class).getInterceptorMetadataRegistry().getInterceptorClassMetadata(ReflectiveClassReference.of(clazz), false);
         return new DirectClassInterceptionHandler<T>(interceptorInstance, interceptionMetadata);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }
}
