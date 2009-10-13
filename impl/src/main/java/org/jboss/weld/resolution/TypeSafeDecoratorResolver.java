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
package org.jboss.weld.resolution;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.Reflections;

/**
 * @author pmuir
 *
 */
public class TypeSafeDecoratorResolver extends TypeSafeBeanResolver<DecoratorImpl<?>>
{

   public TypeSafeDecoratorResolver(BeanManagerImpl manager, Iterable<DecoratorImpl<?>> decorators)
   {
      super(manager, decorators);
   }

   @Override
   protected boolean matches(Resolvable resolvable, DecoratorImpl<?> bean)
   {
      return Reflections.matches(bean.getDelegateTypes(), resolvable.getTypeClosure()) && Beans.containsAllBindings(bean.getDelegateQualifiers(), resolvable.getQualifiers(), getManager()) && getManager().getEnabledDecoratorClasses().contains(bean.getType());
   }
   
   @Override
   protected Set<DecoratorImpl<?>> sortResult(Set<DecoratorImpl<?>> matchedDecorators)
   {
      Set<DecoratorImpl<?>> sortedBeans = new TreeSet<DecoratorImpl<?>>(new Comparator<DecoratorImpl<?>>()
      {
         
         public int compare(DecoratorImpl<?> o1, DecoratorImpl<?> o2)
         {
            List<Class<?>> enabledDecorators = getManager().getEnabledDecoratorClasses();
            int p1 = enabledDecorators.indexOf(((DecoratorImpl<?>) o1).getType());
            int p2 = enabledDecorators.indexOf(((DecoratorImpl<?>) o2).getType());
            return p1 - p2;
         }
   
      });
      sortedBeans.addAll(matchedDecorators);
      return sortedBeans;
   }

}
