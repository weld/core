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

package org.jboss.weld.bean;

import static org.jboss.weld.logging.messages.BeanMessage.CONFLICTING_INTERCEPTOR_BINDINGS;
import static org.jboss.weld.logging.messages.BeanMessage.MISSING_BINDING_ON_INTERCEPTOR;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.interceptor.model.InterceptorMetadata;
import org.jboss.interceptor.proxy.DirectClassInterceptionHandler;
import org.jboss.weld.bean.interceptor.InterceptionMetadataService;
import org.jboss.weld.bean.interceptor.WeldClassReference;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T>
{
   
   private final InterceptorMetadata interceptorClassMetadata;

   private final Set<Annotation> interceptorBindingTypes;
   
   private final boolean serializable;
   
   public static <T> InterceptorImpl<T> of(WeldClass<T> type, BeanManagerImpl beanManager)
   {
      return new InterceptorImpl<T>(type, beanManager);
   }

   protected InterceptorImpl(WeldClass<T> type, BeanManagerImpl beanManager)
   {
      super(type, new StringBuilder().append(Interceptor.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(type.getName()).toString(), beanManager);
      this.interceptorClassMetadata = beanManager.getServices().get(InterceptionMetadataService.class).getInterceptorMetadataRegistry().getInterceptorClassMetadata(WeldClassReference.of(type));
      this.serializable = type.isSerializable();
      this.interceptorBindingTypes = new HashSet<Annotation>();
      interceptorBindingTypes.addAll(flattenInterceptorBindings(beanManager, getWeldAnnotated().getAnnotations()));
      for (Class<? extends Annotation> annotation : getStereotypes())
      {
         interceptorBindingTypes.addAll(flattenInterceptorBindings(beanManager, beanManager.getStereotypeDefinition(annotation)));
      }
      if (this.interceptorBindingTypes.size() == 0)
      {
         throw new DeploymentException(MISSING_BINDING_ON_INTERCEPTOR, type.getName());
      }
      if (Beans.findInterceptorBindingConflicts(beanManager, interceptorBindingTypes))
      {
         throw new DeploymentException(CONFLICTING_INTERCEPTOR_BINDINGS, getType());
      }
   }

   public Set<Annotation> getInterceptorBindings()
   {
      return interceptorBindingTypes;
   }

   public Object intercept(InterceptionType type, T instance, InvocationContext ctx)
   {
      try
      {
         return new DirectClassInterceptionHandler<T>(instance, interceptorClassMetadata).invoke(ctx.getTarget(), org.jboss.interceptor.model.InterceptionType.valueOf(type.name()), ctx);
      } catch (Exception e)
      {
         throw new WeldException(e);
      }
   }

   public boolean intercepts(InterceptionType type)
   {
      return interceptorClassMetadata.getInterceptorMethods(org.jboss.interceptor.model.InterceptionType.valueOf(type.name())).size() > 0;
   }

   public boolean isSerializable()
   {
      return serializable;
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
