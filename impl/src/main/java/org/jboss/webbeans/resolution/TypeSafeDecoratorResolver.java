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
package org.jboss.webbeans.resolution;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Reflections;

/**
 * @author pmuir
 *
 */
public class TypeSafeDecoratorResolver extends TypeSafeBeanResolver<DecoratorBean<?>>
{

   public TypeSafeDecoratorResolver(BeanManagerImpl manager, Iterable<DecoratorBean<?>> decorators)
   {
      super(manager, decorators);
   }

   @Override
   protected boolean matches(Resolvable resolvable, DecoratorBean<?> bean)
   {
      return Reflections.isAssignableFrom(bean.getDelegateTypes(), resolvable.getTypeClosure()) && Beans.containsAllBindings(bean.getDelegateQualifiers(), resolvable.getQualifiers(), getManager()) && getManager().getEnabledDecoratorClasses().contains(bean.getType());
   }
   
   @Override
   protected Set<DecoratorBean<?>> sortResult(Set<DecoratorBean<?>> matchedDecorators)
   {
      Set<DecoratorBean<?>> sortedBeans = new TreeSet<DecoratorBean<?>>(new Comparator<DecoratorBean<?>>()
      {
         
         public int compare(DecoratorBean<?> o1, DecoratorBean<?> o2)
         {
            List<Class<?>> enabledDecorators = getManager().getEnabledDecoratorClasses();
            int p1 = enabledDecorators.indexOf(((DecoratorBean<?>) o1).getType());
            int p2 = enabledDecorators.indexOf(((DecoratorBean<?>) o2).getType());
            return p1 - p2;
         }
   
      });
      sortedBeans.addAll(matchedDecorators);
      return sortedBeans;
   }

}
