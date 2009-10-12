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

package org.jboss.weld.bean.interceptor;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.ejb.spi.InterceptorBindings;
import org.jboss.weld.context.SerializableContextual;
import org.jboss.interceptor.model.InterceptionModel;

/**
 * @author Marius Bogoevici
 */
public class InterceptorBindingsAdapter implements InterceptorBindings
{

   private InterceptionModel<Class<?>, SerializableContextual<Interceptor<?>, ?>> interceptionModel;

   public InterceptorBindingsAdapter(InterceptionModel<Class<?>, SerializableContextual<Interceptor<?>, ?>> interceptionModel)
   {
      if (interceptionModel == null)
      {
         throw new IllegalArgumentException("Interception model must not be null");
      }
      this.interceptionModel = interceptionModel;
   }

   public Collection<Interceptor<?>> getAllInterceptors()
   {
      Collection<SerializableContextual<Interceptor<?>, ?>> contextualSet = interceptionModel.getAllInterceptors();
      return toInterceptorList(contextualSet);
   }

   public List<Interceptor<?>> getMethodInterceptors(InterceptionType interceptionType, Method method)
   {
      if (interceptionType == null)
      {
         throw new IllegalArgumentException("InterceptionType must not be null");
      }

      if (method == null)
      {
         throw new IllegalArgumentException("Method must not be null");
      }

      org.jboss.interceptor.model.InterceptionType internalInterceptionType = org.jboss.interceptor.model.InterceptionType.valueOf(interceptionType.name());

      if (internalInterceptionType.isLifecycleCallback())
      {
         throw new IllegalArgumentException("Interception type must not be lifecycle, but it is " + interceptionType.name());
      }

      return toInterceptorList(interceptionModel.getInterceptors(internalInterceptionType, method));

   }

   public List<Interceptor<?>> getLifecycleInterceptors(InterceptionType interceptionType)
   {
      if (interceptionType == null)
      {
         throw new IllegalArgumentException("InterceptionType must not be null");
      }

      org.jboss.interceptor.model.InterceptionType internalInterceptionType = org.jboss.interceptor.model.InterceptionType.valueOf(interceptionType.name());

      if (!internalInterceptionType.isLifecycleCallback())
      {
         throw new IllegalArgumentException("Interception type must be lifecycle, but it is " + interceptionType.name());
      }

      return toInterceptorList(interceptionModel.getInterceptors(internalInterceptionType, null));
   }

   private List<Interceptor<?>> toInterceptorList(Collection<SerializableContextual<Interceptor<?>, ?>> contextualSet)
   {
      ArrayList<Interceptor<?>> interceptors = new ArrayList<Interceptor<?>>();
      for (SerializableContextual<Interceptor<?>, ?> serializableContextual : contextualSet)
      {
         interceptors.add(serializableContextual.get());
      }
      return interceptors;
   }

}
