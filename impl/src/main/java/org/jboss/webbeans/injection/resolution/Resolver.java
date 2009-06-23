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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.standard.EventBean;
import org.jboss.webbeans.bean.standard.InstanceBean;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.util.Beans;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.collections.ConcurrentCache;

/**
 * Implementation of Web Beans type safe and name based bean resolution
 * 
 * @author Pete Muir
 */
public class Resolver
{
   private static final long serialVersionUID = 1L;
   
   protected static abstract class MatchingResolvable extends ForwardingResolvable
   {
      
      private final BeanManagerImpl manager;
      
      public MatchingResolvable(BeanManagerImpl manager)
      {
         this.manager = manager;
      }
      
      public boolean matches(Set<Type> types, Set<Annotation> bindings)
      {
         return Reflections.isAssignableFrom(this.getTypeClosure(), types) && Beans.containsAllBindings(this.getBindings(), bindings, manager);
      }
      
      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof Resolvable)
         {
            Resolvable that = (Resolvable) obj;
            return this.matches(that.getTypeClosure(), that.getBindings());
         }
         else
         {
            return false;
         }
      }
      
   }
   
   // The resolved injection points
   private ConcurrentCache<Resolvable, Set<Bean<?>>> resolvedInjectionPoints;
   // The registerd injection points
   private Set<WBAnnotated<?, ?>> injectionPoints;
   // The resolved names
   private ConcurrentCache<String, Set<Bean<?>>> resolvedNames;
   
   // The beans to search
   private final List<? extends Bean<?>> allBeans;
   
   // The Web Beans manager
   private final BeanManagerImpl manager;
   
   // Annotation transformers used to mutate annotations during resolution
   private final Set<ResolvableTransformer> transformers;

   /**
    * Constructor
    * 
    */
   public Resolver(BeanManagerImpl manager, List<? extends Bean<?>> allBeans)
   {
      this.manager = manager;
      this.allBeans = allBeans;
      this.injectionPoints = new HashSet<WBAnnotated<?, ?>>();
      this.resolvedInjectionPoints = new ConcurrentCache<Resolvable, Set<Bean<?>>>();
      this.resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
      this.transformers = new HashSet<ResolvableTransformer>();
      transformers.add(EventBean.TRANSFORMER);
      transformers.add(InstanceBean.TRANSFORMER);
   }
   /**
    * Add multiple injection points for later resolving using
    * {@link #registerInjectionPoint(WBAnnotated)}. Useful during bootstrap.
    * 
    * @param elements The injection points to add
    */
   public void addInjectionPoints(Collection<? extends WBAnnotated<?, ?>> elements)
   {
      injectionPoints.addAll(elements);
   }

   /**
    * Registers an injection point
    * 
    * @param <T>
    * @param <S>
    * @param element The injection point to add
    * @return A set of matching beans for the injection point
    */
   private Set<Bean<?>> registerInjectionPoint(final Resolvable element)
   {
      final MatchingResolvable wrapped = new MatchingResolvable(manager)
      {

         @Override
         protected Resolvable delegate()
         {
            return element;
         }

      };
      Callable<Set<Bean<?>>> callable = new Callable<Set<Bean<?>>>()
      {
         public Set<Bean<?>> call() throws Exception
         {
            Set<Bean<?>> matchedBeans = getMatchingBeans(wrapped);
            matchedBeans = retainHighestPrecedenceBeans(matchedBeans);
            return sortBeans(matchedBeans);
         }

      };
      return resolvedInjectionPoints.putIfAbsent(wrapped, callable);
   }

   /**
    * Reset all cached injection points. You must reset all cached injection
    * points when you add a bean to the manager
    */
   public void clear()
   {
      this.resolvedInjectionPoints = new ConcurrentCache<Resolvable, Set<Bean<?>>>();
      resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
   }

   /**
    * Resolve all injection points added using
    * {@link #addInjectionPoints(Collection)}
    */
   public void resolveInjectionPoints()
   {
      for (final WBAnnotated<? extends Object, ? extends Object> injectable : injectionPoints)
      {
         registerInjectionPoint(ResolvableFactory.of(injectable));
      }
   }

   /**
    * Get the possible beans for the given element
    * 
    * @param key The resolving criteria
    * @return An unmodifiable set of matching beans
    */
   public Set<Bean<?>> get(final Resolvable key)
   {
      Set<Bean<?>> beans = registerInjectionPoint(transformElement(key));
      return Collections.unmodifiableSet(beans);
   }
   
   private Resolvable transformElement(Resolvable element)
   {
      for (ResolvableTransformer transformer : transformers)
      {
         element = transformer.transform(element);
      }
      return element;
   }

   /**
    * Get the possible beans for the given name
    * 
    * @param name The name to match
    * @return The set of matching beans
    */
   public Set<Bean<?>> get(final String name)
   {
      return resolvedNames.putIfAbsent(name, new Callable<Set<Bean<?>>>()
      {

         public Set<Bean<? extends Object>> call() throws Exception
         {
            Set<Bean<?>> matchedBeans = new HashSet<Bean<?>>();
            for (Bean<?> bean : allBeans)
            {
               if ((bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
               {
                  matchedBeans.add(bean);
               }
            }
            matchedBeans = retainHighestPrecedenceBeans(matchedBeans);
            return sortBeans(matchedBeans);
         }

      });
   }
   

   protected Set<Bean<?>> sortBeans(Set<Bean<?>> matchedBeans)
   {
      return matchedBeans;
   }

   /**
    * Filters out the beans with the highest enabled deployment type
    * 
    * @param <T>
    * @param beans The beans to filter
    * @param enabledDeploymentTypes The enabled deployment types
    * @return The filtered beans
    */
   protected Set<Bean<?>> retainHighestPrecedenceBeans(Set<Bean<?>> beans)
   {
      if (beans.size() > 0)
      {
         SortedSet<Class<? extends Annotation>> possibleDeploymentTypes = new TreeSet<Class<? extends Annotation>>(new ListComparator<Class<? extends Annotation>>(manager.getEnabledDeploymentTypes()));
         for (Bean<?> bean : beans)
         {
            possibleDeploymentTypes.add(bean.getDeploymentType());
         }
         possibleDeploymentTypes.retainAll(manager.getEnabledDeploymentTypes());
         Set<Bean<?>> trimmed = new HashSet<Bean<?>>();
         if (possibleDeploymentTypes.size() > 0)
         {
            Class<? extends Annotation> highestPrecedencePossibleDeploymentType = possibleDeploymentTypes.last();

            for (Bean<?> bean : beans)
            {
               if (bean.getDeploymentType().equals(highestPrecedencePossibleDeploymentType))
               {
                  trimmed.add(bean);
               }
            }
         }
         return trimmed;
      }
      else
      {
         return beans;
      }
   }

   /**
    * Gets the matching beans for binding criteria from a list of beans
    * 
    * @param <T> The type of the beans
    * @param element The binding criteria
    * @param beans The beans to filter
    * @return A set of filtered beans
    */
   private Set<Bean<?>> getMatchingBeans(MatchingResolvable resolvable)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : allBeans)
      {
         if (matches(resolvable, bean))
         {
            resolvedBeans.add(bean);
         }
      }
      return resolvedBeans;
   }

   protected boolean matches(MatchingResolvable resolvable, Bean<?> bean)
   {
      return resolvable.matches(bean.getTypes(), bean.getBindings());
   }

   /**
    * Gets a string representation
    * 
    * @return A string representation
    */
   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Resolver\n");
      buffer.append("Injection points: " + injectionPoints.size() + "\n");
      buffer.append("Resolved injection points: " + resolvedInjectionPoints.size() + "\n");
      buffer.append("Resolved names points: " + resolvedNames.size() + "\n");
      return buffer.toString();
   }

   protected BeanManagerImpl getManager()
   {
      return manager;
   }

}
