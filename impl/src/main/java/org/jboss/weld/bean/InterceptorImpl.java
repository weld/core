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

package org.jboss.weld.bean;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.interceptor.model.InterceptorClassMetadata;
import org.jboss.interceptor.proxy.DirectClassInterceptionHandler;
import org.jboss.interceptor.registry.InterceptorClassMetadataRegistry;
import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.DeploymentException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.util.Beans;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T>
{
   private final InterceptorClassMetadata interceptorClassMetadata;

   private final Set<Annotation> interceptorBindingTypes;

   protected InterceptorImpl(WeldClass<T> type, BeanManagerImpl manager)
   {
      super(type, new StringBuilder().append(Interceptor.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(type.getName()).toString(), manager);
      this.interceptorClassMetadata = InterceptorClassMetadataRegistry.getRegistry().getInterceptorClassMetadata(type.getJavaClass());
      this.interceptorBindingTypes = new HashSet<Annotation>();
      interceptorBindingTypes.addAll(flattenInterceptorBindings(manager, getAnnotatedItem().getAnnotations()));
      for (Class<? extends Annotation> annotation : getStereotypes())
      {
         interceptorBindingTypes.addAll(flattenInterceptorBindings(manager, manager.getStereotypeDefinition(annotation)));
      }
      if (this.interceptorBindingTypes.size() == 0)
      {
         throw new DeploymentException("An interceptor must have at least one binding, but " + type.getName() + " has none");
      }
      if (Beans.findInterceptorBindingConflicts(manager, interceptorBindingTypes))
      {
         throw new DeploymentException("Conflicting interceptor bindings found on " + getType());
      }
   }

   public static <T> InterceptorImpl<T> of(WeldClass<T> type, BeanManagerImpl manager)
   {
      return new InterceptorImpl<T>(type, manager);
   }

   public Set<Annotation> getInterceptorBindings()
   {
      return interceptorBindingTypes;
   }

   public Object intercept(InterceptionType type, T instance, InvocationContext ctx)
   {
      try
      {
         return new DirectClassInterceptionHandler(instance, getType()).invoke(ctx.getTarget(), org.jboss.interceptor.model.InterceptionType.valueOf(type.name()), ctx);
      } catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean intercepts(InterceptionType type)
   {
      return interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.valueOf(type.name())).size() > 0;
   }

   @Override
   protected void defaultPostConstruct(T instance) 
   {
      // Lifecycle callbacks not supported
   }
   
   @Override
   protected void defaultPreDestroy(T instance) 
   {
      // Lifecycle callbacks not supported
   }
   
}
