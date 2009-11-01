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

package org.jboss.weld.resolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.util.Beans;

/**
 * @author <a href="mailto:mariusb@redhat.com">Marius Bogoevici</a>
 */
public class TypeSafeInterceptorResolver extends TypeSafeResolver<InterceptorResolvable,InterceptorImpl<?>>
{
   private BeanManagerImpl manager;


   public TypeSafeInterceptorResolver(BeanManagerImpl manager, Iterable<InterceptorImpl<?>> interceptors)
   {
      super(interceptors);
      this.manager = manager;
   }

   @Override
   protected boolean matches(InterceptorResolvable resolvable, InterceptorImpl<?> bean)
   {
      return bean.intercepts(resolvable.getInterceptionType())
            && bean.getInterceptorBindings().size() > 0
            && Beans.containsAllInterceptionBindings(bean.getInterceptorBindings(), resolvable.getQualifiers(), getManager())
            && getManager().getEnabledInterceptorClasses().contains(bean.getType());
   }


   @Override
   protected Set<InterceptorImpl<?>> sortResult(Set<InterceptorImpl<?>> matchedInterceptors)
   {
      Set<InterceptorImpl<?>> sortedBeans = new TreeSet<InterceptorImpl<?>>(new Comparator<InterceptorImpl<?>>()
      {

         public int compare(InterceptorImpl<?> o1, InterceptorImpl<?> o2)
         {
            List<Class<?>> enabledInterceptors = getManager().getEnabledInterceptorClasses();
            int p1 = enabledInterceptors.indexOf(((InterceptorImpl<?>) o1).getType());
            int p2 = enabledInterceptors.indexOf(((InterceptorImpl<?>) o2).getType());
            return p1 - p2;
         }

      });
      sortedBeans.addAll(matchedInterceptors);
      return sortedBeans;
   }

   @Override
   protected Set<InterceptorImpl<?>> filterResult(Set<InterceptorImpl<?>> matched)
   {
      return matched;
   }

   @Override
   protected Iterable<ResolvableTransformer> getTransformers()
   {
      return Collections.emptySet();
   }

   public BeanManagerImpl getManager()
   {
      return manager;
   }
}
