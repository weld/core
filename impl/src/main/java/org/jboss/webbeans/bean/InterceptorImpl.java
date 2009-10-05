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

package org.jboss.webbeans.bean;

import org.jboss.interceptor.model.InterceptorClassMetadata;
import org.jboss.interceptor.registry.InterceptorClassMetadataRegistry;
import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;

import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Marius Bogoevici
 */
public class InterceptorImpl<T> extends ManagedBean<T> implements Interceptor<T>
{
   private final InterceptorClassMetadata interceptorClassMetadata;

   private final Set<Annotation> interceptorBindingTypes;

   protected InterceptorImpl(WBClass<T> type, BeanManagerImpl manager)
   {
      super(type, new StringBuilder().append(Interceptor.class.getSimpleName()).append(BEAN_ID_SEPARATOR).append(type.getName()).toString(), manager);
      this.interceptorClassMetadata = InterceptorClassMetadataRegistry.getRegistry().getInterceptorClassMetadata(type.getJavaClass());
      this.interceptorBindingTypes = new HashSet<Annotation>();
      for (Annotation annotation: getAnnotatedItem().getAnnotations())
      {
         if (manager.isInterceptorBindingType(annotation.annotationType()))
         {
            interceptorBindingTypes.add(annotation);
            interceptorBindingTypes.addAll(getManager().getServices().get(MetaAnnotationStore.class).getInterceptorBindingModel(annotation.annotationType()).getInheritedInterceptionBindingTypes());
         }
      }
      
   }

   public static <T> InterceptorImpl<T> of(WBClass<T> type, BeanManagerImpl manager)
   {
      return new InterceptorImpl(type, manager);
   }

   public Set<Annotation> getInterceptorBindingTypes()
   {
      return interceptorBindingTypes;
   }

   public Object intercept(InterceptionType type, T instance, InvocationContext ctx)
   {
      try
      {
         return ctx.proceed();
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
   public void postConstruct(T instance)
   {
      // do nothing on PostConstruct
   }

   @Override
   public void preDestroy(T instance)
   {
      // do nothing on PreDestroy
   }
}
