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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

/**
 * @author pmuir
 * 
 */
public class TypeSafeBeanResolver<T extends Bean<?>> extends TypeSafeResolver<Resolvable, T>
{

   private final BeanManagerImpl beanManager;
   private final ConcurrentMap<Set<Bean<?>>, Set<Bean<?>>> disambiguatedBeans;

   public static class BeanDisambiguation implements Function<Set<Bean<?>>, Set<Bean<?>>>
   {

      private BeanDisambiguation()
      {
      }

      public Set<Bean<?>> apply(Set<Bean<?>> from)
      {
         if (from.size() > 1)
         {
            boolean alternativePresent = Beans.isAlternativePresent(from);
            Set<Bean<?>> disambiguatedBeans = new HashSet<Bean<?>>();

            for (Bean<?> bean : from)
            {
               if (alternativePresent ? bean.isAlternative() : true)
               {
                  disambiguatedBeans.add(bean);
               }
            }
            return disambiguatedBeans;
         }
         else
         {
            return from;
         }
      }

   }

   public TypeSafeBeanResolver(BeanManagerImpl beanManager, Iterable<T> beans)
   {
      super(beans);
      this.beanManager = beanManager;
      this.disambiguatedBeans = new MapMaker().makeComputingMap(new BeanDisambiguation());
   }

   @Override
   protected boolean matches(Resolvable resolvable, T bean)
   {
      return Reflections.matches(resolvable.getTypes(), bean.getTypes()) && Beans.containsAllQualifiers(resolvable.getQualifiers(), bean.getQualifiers(), beanManager);
   }

   /**
    * @return the manager
    */
   protected BeanManagerImpl getBeanManager()
   {
      return beanManager;
   }

   @Override
   protected Set<T> filterResult(Set<T> matched)
   {
      return Beans.removeDisabledAndSpecializedBeans(matched, beanManager);
   }

   @Override
   protected Set<T> sortResult(Set<T> matched)
   {
      return matched;
   }

   public <X> Set<Bean<? extends X>> resolve(Set<Bean<? extends X>> beans)
   {
      if (beans.size() <= 1)
      {
         return beans;
      }
      /*
       * We need to defensively copy the beans set as it can be provided by
       * the user in which case this algorithm will have thread safety issues
       */
      beans = new HashSet<Bean<? extends X>>(beans);
      return cast(Collections.unmodifiableSet(disambiguatedBeans.get(beans)));
   }

   @Override
   public void clear()
   {
      super.clear();
      this.disambiguatedBeans.clear();
   }

}
