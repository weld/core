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

package org.jboss.webbeans;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import javax.webbeans.NullableDependencyException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Decorator;
import javax.webbeans.manager.InterceptionType;
import javax.webbeans.manager.Interceptor;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;
import org.jboss.webbeans.model.BindingTypeModel;
import org.jboss.webbeans.util.ConcurrentCache;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.Strings;

/**
 * Implementation of Web Beans type safe and name based bean resolution
 * 
 * @author Pete Muir
 */
public class Resolver
{

   /**
    * Extension of an element which bases equality not only on type, but also on
    * binding type
    */
   private abstract class ResolvableAnnotatedItem<T, S> extends ForwardingAnnotatedItem<T, S>
   {

      @Override
      public boolean equals(Object other)
      {
         // TODO Do we need to check the other direction too?
         if (other instanceof AnnotatedItem)
         {
            AnnotatedItem<?, ?> that = (AnnotatedItem<?, ?>) other;
            return delegate().isAssignableFrom(that) && that.getBindingTypes().equals(this.getBindingTypes());
         }
         else
         {
            return false;
         }
      }

      @Override
      public int hashCode()
      {
         return delegate().hashCode();
      }

      @Override
      public String toString()
      {
         StringBuilder buffer = new StringBuilder();
         buffer.append("Resolvable annotation item\n");
         buffer.append(delegate().toString() + "\n");
         return buffer.toString();
      }

   }

   private ConcurrentCache<ResolvableAnnotatedItem<?, ?>, Set<Bean<?>>> resolvedInjectionPoints;
   private Set<AnnotatedItem<?, ?>> injectionPoints;

   private ConcurrentCache<String, Set<Bean<?>>> resolvedNames;

   public Resolver()
   {
      this.injectionPoints = new HashSet<AnnotatedItem<?, ?>>();
      this.resolvedInjectionPoints = new ConcurrentCache<ResolvableAnnotatedItem<?,?>, Set<Bean<?>>>();
      this.resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
   }

   /**
    * Add multiple injection points for later resolving using
    * {@link #registerInjectionPoint(AnnotatedItem)}. Useful during bootstrap.
    */
   public void addInjectionPoints(Collection<AnnotatedItem<?, ?>> elements)
   {
      injectionPoints.addAll(elements);
   }

   private <T, S> Set<Bean<T>> registerInjectionPoint(final ResolvableAnnotatedItem<T, S> element)
   {
      Callable<Set<Bean<T>>> callable = new Callable<Set<Bean<T>>>()
      {

         public Set<Bean<T>> call() throws Exception
         {
            Set<Bean<T>> beans = retainHighestPrecedenceBeans(getMatchingBeans(element, ManagerImpl.instance().getBeans(), ManagerImpl.instance().getMetaDataCache()), ManagerImpl.instance().getEnabledDeploymentTypes());
            if (element.getType().isPrimitive())
            {
               for (Bean<?> bean : beans)
               {
                  if (bean.isNullable())
                  {
                     throw new NullableDependencyException("Primitive injection points resolves to nullable web bean");
                  }
               }
            }
            return beans;
         }

      };
      return resolvedInjectionPoints.putIfAbsent(element, callable);
   }

   /**
    * Reset all cached injection points. You must reset all cached injection
    * points when you add a bean to the manager
    */
   public void clear()
   {
      this.resolvedInjectionPoints = new ConcurrentCache<ResolvableAnnotatedItem<?,?>, Set<Bean<?>>>();
      resolvedNames = new ConcurrentCache<String, Set<Bean<?>>>();
   }

   /**
    * Resolve all injection points added using
    * {@link #addInjectionPoints(Collection)}
    */
   @SuppressWarnings("unchecked")
   public void resolveInjectionPoints()
   {
      for (final AnnotatedItem injectable : injectionPoints)
      {
         registerInjectionPoint(new ResolvableAnnotatedItem<Object, Object>()
         {

            @Override
            public AnnotatedItem<Object, Object> delegate()
            {
               return injectable;
            }
         });
      }
   }

   /**
    * Get the possible beans for the given element
    */
   @SuppressWarnings("unchecked")
   public <T, S> Set<Bean<T>> get(final AnnotatedItem<T, S> key)
   {
      Set<Bean<T>> beans = new HashSet<Bean<T>>();

      final ResolvableAnnotatedItem<T, S> element = new ResolvableAnnotatedItem<T, S>()
      {

         @Override
         public AnnotatedItem<T, S> delegate()
         {
            return key;
         }

      };

      if (element.getType().equals(Object.class))
      {
         // TODO Fix this cast
         beans = new HashSet<Bean<T>>((List) ManagerImpl.instance().getBeans());
      }
      else
      {
         beans = registerInjectionPoint(element);
      }
      return Collections.unmodifiableSet(beans);
   }

   /**
    * Get the possible beans for the given name
    */
   public Set<Bean<?>> get(final String name)
   {
      return resolvedNames.putIfAbsent(name, new Callable<Set<Bean<?>>>()
      {

         @SuppressWarnings("unchecked")
         public Set<Bean<?>> call() throws Exception
         {
            Set<Bean<?>> beans = new HashSet<Bean<?>>();
            for (Bean<?> bean : ManagerImpl.instance().getBeans())
            {
               if ((bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
               {
                  beans.add(bean);
               }
            }
            return retainHighestPrecedenceBeans((Set) beans, ManagerImpl.instance().getEnabledDeploymentTypes());
         }

      });
   }

   private static <T> Set<Bean<T>> retainHighestPrecedenceBeans(Set<Bean<T>> beans, List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      if (beans.size() > 0)
      {
         SortedSet<Class<? extends Annotation>> possibleDeploymentTypes = new TreeSet<Class<? extends Annotation>>(new ListComparator<Class<? extends Annotation>>(enabledDeploymentTypes));
         for (Bean<?> bean : beans)
         {
            possibleDeploymentTypes.add(bean.getDeploymentType());
         }
         possibleDeploymentTypes.retainAll(enabledDeploymentTypes);
         Set<Bean<T>> trimmed = new HashSet<Bean<T>>();
         if (possibleDeploymentTypes.size() > 0)
         {
            Class<? extends Annotation> highestPrecedencePossibleDeploymentType = possibleDeploymentTypes.last();

            for (Bean<T> bean : beans)
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

   @SuppressWarnings("unchecked")
   private static <T> Set<Bean<T>> getMatchingBeans(AnnotatedItem<T, ?> element, List<Bean<?>> beans, MetaDataCache metaDataCache)
   {
      Set<Bean<T>> resolvedBeans = new HashSet<Bean<T>>();
      for (Bean<?> bean : beans)
      {
         if (element.isAssignableFrom(bean.getTypes()) && containsAllBindingBindingTypes(element, bean.getBindingTypes(), metaDataCache))
         {
            resolvedBeans.add((Bean<T>) bean);
         }
      }
      return resolvedBeans;
   }

   private static boolean containsAllBindingBindingTypes(AnnotatedItem<?, ?> element, Set<Annotation> bindingTypes, MetaDataCache metaDataCache)
   {
      for (Annotation bindingType : element.getBindingTypes())
      {
         BindingTypeModel<?> bindingTypeModel = metaDataCache.getBindingTypeModel(bindingType.annotationType());
         if (bindingTypeModel.getNonBindingTypes().size() > 0)
         {
            boolean matchFound = false;
            for (Annotation otherBindingType : bindingTypes)
            {
               if (bindingTypeModel.isEqual(bindingType, otherBindingType))
               {
                  matchFound = true;
               }
            }
            if (!matchFound)
            {
               return false;
            }
         }
         else if (!bindingTypes.contains(bindingType))
         {
            return false;
         }
      }
      return true;
   }

   public List<Decorator> resolveDecorators(Set<Class<?>> types, Annotation[] bindingTypes)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public List<Interceptor> resolveInterceptors(InterceptionType type, Annotation[] interceptorBindings)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String toString()
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Resolver\n");
      buffer.append(resolvedInjectionPoints.toString() + "\n");
      buffer.append(Strings.collectionToString("Injection points: ", injectionPoints));
      buffer.append(Strings.mapToString("Resolved names: ", resolvedNames));
      return buffer.toString();
   }

}
