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
package org.jboss.weld.resolution;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 *
 */
public class TypeSafeDecoratorResolver extends TypeSafeBeanResolver<Decorator<?>>
{
   
   public TypeSafeDecoratorResolver(BeanManagerImpl manager, Iterable<Decorator<?>> decorators)
   {
      super(manager, decorators);
   }

   @Override
   protected boolean matches(Resolvable resolvable, Decorator<?> bean)
   {
      return Reflections.matches(Collections.singleton(bean.getDelegateType()), resolvable.getTypes())
            && Beans.containsAllBindings(bean.getDelegateQualifiers(), resolvable.getQualifiers(), getBeanManager())
            && getBeanManager().getEnabledDecoratorClasses().contains(bean.getBeanClass());
   }
   
   @Override
   protected Set<Decorator<?>> sortResult(Set<Decorator<?>> matchedDecorators)
   {
      Set<Decorator<?>> sortedBeans = new TreeSet<Decorator<?>>(new Comparator<Decorator<?>>()
      {
         
         public int compare(Decorator<?> o1, Decorator<?> o2)
         {
            List<Class<?>> enabledDecorators = getBeanManager().getEnabledDecoratorClasses();
            int p1 = enabledDecorators.indexOf(((Decorator<?>) o1).getBeanClass());
            int p2 = enabledDecorators.indexOf(((Decorator<?>) o2).getBeanClass());
            return p1 - p2;
         }
   
      });
      sortedBeans.addAll(matchedDecorators);
      return sortedBeans;
   }

}
