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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.builtin.facade.EventBean;
import org.jboss.webbeans.bean.builtin.facade.InstanceBean;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.collections.ConcurrentCache;

/**
 * @author pmuir
 *
 */
public class TypeSafeBeanResolver<T extends Bean<?>> extends TypeSafeResolver<T>
{

   public static final Set<ResolvableTransformer> TRANSFORMERS;
   
   private final BeanManagerImpl manager;
   private final ConcurrentCache<Set<?>, Set<Bean<?>>> disambiguatedBeans; 
   
   static
   {
      TRANSFORMERS = new HashSet<ResolvableTransformer>();
      TRANSFORMERS.add(EventBean.TRANSFORMER);
      TRANSFORMERS.add(InstanceBean.TRANSFORMER);
      TRANSFORMERS.add(new NewResolvableTransformer());
   }

   public TypeSafeBeanResolver(BeanManagerImpl manager, Iterable<T> beans)
   {
      super(beans);
      this.manager = manager;
      this.disambiguatedBeans = new ConcurrentCache<Set<?>, Set<Bean<?>>>();
   }

   @Override
   protected boolean matches(Resolvable resolvable, T bean)
   {
      return Reflections.isAssignableFrom(resolvable.getTypeClosure(), bean.getTypes()) && Beans.containsAllBindings(resolvable.getBindings(), bean.getBindings(), manager);
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
      return Beans.retainEnabledPolicies(matched, manager.getEnabledPolicyClasses(), manager.getEnabledPolicyStereotypes());
   }

   @Override
   protected Iterable<ResolvableTransformer> getTransformers()
   {
      return TRANSFORMERS;
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
               boolean policyPresent = Beans.isPolicyPresent(disambiguatedBeans);
               disambiguatedBeans = new HashSet<Bean<? extends X>>();
               
               for (Bean<? extends X> bean : beans)
               {
                  if (policyPresent ? bean.isPolicy() : true && !Beans.isSpecialized(bean, beans, manager.getSpecializedBeans()))
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
