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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.util.TypeLiteral;
import javax.inject.Provider;

import org.jboss.weld.bean.builtin.FacadeBeanResolvableTransformer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ConcurrentCache;
import org.jboss.weld.util.reflection.Reflections;

/**
 * @author pmuir
 *
 */
public class TypeSafeBeanResolver<T extends Bean<?>> extends TypeSafeResolver<Resolvable, T>
{
   
   private static final Class<Instance<?>> INSTANCE_TYPE = new TypeLiteral<Instance<?>>() {}.getRawType();
   private static final Class<Provider<?>> PROVIDER_TYPE = new TypeLiteral<Provider<?>>() {}.getRawType();
   private static final Class<Event<?>> EVENT_TYPE = new TypeLiteral<Event<?>>() {}.getRawType();

   private final Set<ResolvableTransformer> transformers;
   private final BeanManagerImpl manager;
   private final ConcurrentCache<Set<?>, Set<Bean<?>>> disambiguatedBeans; 

   public TypeSafeBeanResolver(BeanManagerImpl manager, Iterable<T> beans)
   {
      super(beans);
      this.manager = manager;
      this.disambiguatedBeans = new ConcurrentCache<Set<?>, Set<Bean<?>>>();
      transformers = new HashSet<ResolvableTransformer>();
      transformers.add(new FacadeBeanResolvableTransformer(EVENT_TYPE));
      transformers.add(new FacadeBeanResolvableTransformer(INSTANCE_TYPE));
      transformers.add(new FacadeBeanResolvableTransformer(PROVIDER_TYPE));
      transformers.add(new NewResolvableTransformer());
   }

   @Override
   protected boolean matches(Resolvable resolvable, T bean)
   {
      return Reflections.matches(resolvable.getTypeClosure(), bean.getTypes()) && Beans.containsAllBindings(resolvable.getQualifiers(), bean.getQualifiers(), manager);
   }
   
   /**
    * @return the manager
    */
   public BeanManagerImpl getManager()
   {
      return manager;
   }
   
   @Override
   protected Set<T> filterResult(Set<T> matched)
   {
      return Beans.retainEnabledAlternatives(matched, manager.getEnabledAlternativeClasses(), manager.getEnabledAlternativeStereotypes());
   }

   @Override
   protected Iterable<ResolvableTransformer> getTransformers()
   {
      return transformers;
   }

   @Override
   protected Set<T> sortResult(Set<T> matched)
   {
      return matched;
   }
   
   public <X> Set<Bean<? extends X>> resolve(final Set<Bean<? extends X>> beans)
   {
      return disambiguatedBeans.<Set<Bean<? extends X>>>putIfAbsent(beans, new Callable<Set<Bean<? extends X>>>()
      {

         public Set<Bean<? extends X>> call() throws Exception
         {
            Set<Bean<? extends X>> disambiguatedBeans = beans;
            if (disambiguatedBeans.size() > 1)
            {
               boolean alternativePresent = Beans.isAlternativePresent(disambiguatedBeans);
               disambiguatedBeans = new HashSet<Bean<? extends X>>();
               
               for (Bean<? extends X> bean : beans)
               {
                  if (alternativePresent ? bean.isAlternative() : true && !Beans.isSpecialized(bean, beans, manager.getSpecializedBeans()))
                  {
                     disambiguatedBeans.add(bean);
                  }
               }
               
            }
            return disambiguatedBeans;
         }
         
      });
      
   }
   
   public <X> Set<Bean<? extends X>> resolve(final Collection<Bean<? extends X>> beans)
   {
      return resolve(new HashSet<Bean<? extends X>>(beans));
   }


}
