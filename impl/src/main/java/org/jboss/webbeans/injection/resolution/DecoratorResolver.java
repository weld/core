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
package org.jboss.webbeans.injection.resolution;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Reflections;

/**
 * @author pmuir
 *
 */
public class DecoratorResolver extends Resolver
{

   public DecoratorResolver(BeanManagerImpl manager, List<? extends Bean<?>> beans)
   {
      super(manager, beans);
   }

   @Override
   protected boolean matches(MatchingResolvable resolvable, Bean<?> bean)
   {
      if (bean instanceof DecoratorBean)
      {
         DecoratorBean<?> decoratorBean = (DecoratorBean<?>) bean;
         return Reflections.isAssignableFrom(decoratorBean.getDelegateTypes(), resolvable.getTypeClosure()) && Beans.containsAllBindings(decoratorBean.getDelegateBindings(), resolvable.getBindings(), getManager()) && getManager().getEnabledDecoratorClasses().contains(decoratorBean.getType());
      }
      else
      {
         throw new IllegalStateException("Unable to process non container generated decorator!");
      }
   }
   
   @Override
   protected Set<Bean<?>> sortBeans(Set<Bean<?>> matchedBeans)
   {
      Set<Bean<?>> sortedBeans = new TreeSet<Bean<?>>(new Comparator<Bean<?>>()
      {
         
         public int compare(Bean<?> o1, Bean<?> o2)
         {
            if (o1 instanceof DecoratorBean && o2 instanceof DecoratorBean)
            {
               List<Class<?>> enabledDecorators = getManager().getEnabledDecoratorClasses();
               int p1 = enabledDecorators.indexOf(((DecoratorBean<?>) o1).getType());
               int p2 = enabledDecorators.indexOf(((DecoratorBean<?>) o2).getType());
               return p1 - p2;
            }
            else
            {
               throw new IllegalStateException("Unable to process non container generated decorator!");
            }
            
         }
   
      });
      sortedBeans.addAll(matchedBeans);
      return sortedBeans;
   }

}
