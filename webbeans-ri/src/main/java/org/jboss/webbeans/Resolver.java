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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.webbeans.NullableDependencyException;
import javax.webbeans.manager.Bean;
import javax.webbeans.manager.Decorator;
import javax.webbeans.manager.InterceptionType;
import javax.webbeans.manager.Interceptor;

import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.ForwardingAnnotatedItem;
import org.jboss.webbeans.model.BindingTypeModel;
import org.jboss.webbeans.util.ListComparator;
import org.jboss.webbeans.util.Strings;

import com.google.common.collect.ForwardingMap;

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
         StringBuffer buffer = new StringBuffer();
         buffer.append("Resolvable annotation item\n");
         buffer.append(delegate().toString() + "\n");
         return buffer.toString();
      }

   }

   // TODO Why can't we generify Set?

   /**
    * Type safe map for caching annotation metadata
    */
   @SuppressWarnings("unchecked")
   private class AnnotatedItemMap extends ForwardingMap<AnnotatedItem<?, ?>, Set>
   {

      private Map<AnnotatedItem<?, ?>, Set> delegate;

      public AnnotatedItemMap()
      {
         delegate = new HashMap<AnnotatedItem<?, ?>, Set>();
      }

      public <T> Set<Bean<T>> get(AnnotatedItem<T, ?> key)
      {
         return (Set<Bean<T>>) super.get(key);
      }

      @Override
      protected Map<AnnotatedItem<?, ?>, Set> delegate()
      {
         return delegate;
      }
      
      @Override
      public String toString()
      {
         return Strings.mapToString("AnnotatedItemMap (annotated item -> bean set): ", delegate);
      }      

   }

   private AnnotatedItemMap resolvedInjectionPoints;
   private Set<AnnotatedItem<?, ?>> injectionPoints;

   private Map<String, Set<Bean<?>>> resolvedNames;

   private ManagerImpl manager;

   public Resolver(ManagerImpl manager)
   {
      this.manager = manager;
      this.injectionPoints = new HashSet<AnnotatedItem<?, ?>>();
      this.resolvedInjectionPoints = new AnnotatedItemMap();
   }

   /**
    * Add multiple injection points for later resolving using
    * {@link #registerInjectionPoint(AnnotatedItem)}. Useful during bootstrap.
    */
   public void addInjectionPoints(Collection<AnnotatedItem<?, ?>> elements)
   {
      injectionPoints.addAll(elements);
   }

   private <T, S> void registerInjectionPoint(final AnnotatedItem<T, S> element)
   {
      Set<Bean<?>> beans = retainHighestPrecedenceBeans(getMatchingBeans(element, manager.getBeans(), manager.getMetaDataCache()), manager.getEnabledDeploymentTypes());
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
      resolvedInjectionPoints.put(new ResolvableAnnotatedItem<T, S>()
      {

         @Override
         public AnnotatedItem<T, S> delegate()
         {
            return element;
         }

      }, beans);
   }

   /**
    * Reset all cached injection points. You must reset all cached injection
    * points when you add a bean to the manager
    */
   public void clear()
   {
      resolvedInjectionPoints = new AnnotatedItemMap();
      resolvedNames = new HashMap<String, Set<Bean<?>>>();
   }

   /**
    * Resolve all injection points added using
    * {@link #addInjectionPoints(Collection)}
    */
   public void resolveInjectionPoints()
   {
      for (AnnotatedItem<?, ?> injectable : injectionPoints)
      {
         registerInjectionPoint(injectable);
      }
   }

   /**
    * Get the possible beans for the given element
    */
   @SuppressWarnings("unchecked")
   public <T, S> Set<Bean<T>> get(final AnnotatedItem<T, S> key)
   {
      Set<Bean<T>> beans = new HashSet<Bean<T>>();

      AnnotatedItem<T, S> element = new ResolvableAnnotatedItem<T, S>()
      {

         @Override
         public AnnotatedItem<T, S> delegate()
         {
            return key;
         }

      };

      // TODO We don't need this I think
      if (element.getType().equals(Object.class))
      {
         // TODO Fix this cast
         beans = new HashSet<Bean<T>>((List) manager.getBeans());
      }
      else
      {
         if (!resolvedInjectionPoints.containsKey(element))
         {
            registerInjectionPoint(element);
         }
         beans = resolvedInjectionPoints.get(element);
      }
      return Collections.unmodifiableSet(beans);
   }

   /**
    * Get the possible beans for the given name
    */
   public Set<Bean<?>> get(String name)
   {
      Set<Bean<?>> beans;
      if (resolvedNames.containsKey(name))
      {
         beans = resolvedNames.get(name);
      }
      else
      {
         beans = new HashSet<Bean<?>>();
         for (Bean<?> bean : manager.getBeans())
         {
            if ((bean.getName() == null && name == null) || (bean.getName() != null && bean.getName().equals(name)))
            {
               beans.add(bean);
            }
         }
         beans = retainHighestPrecedenceBeans(beans, manager.getEnabledDeploymentTypes());
         resolvedNames.put(name, beans);

      }
      return Collections.unmodifiableSet(beans);
   }

   private static Set<Bean<?>> retainHighestPrecedenceBeans(Set<Bean<?>> beans, List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      if (beans.size() > 0)
      {
         SortedSet<Class<? extends Annotation>> possibleDeploymentTypes = new TreeSet<Class<? extends Annotation>>(new ListComparator<Class<? extends Annotation>>(enabledDeploymentTypes));
         for (Bean<?> bean : beans)
         {
            possibleDeploymentTypes.add(bean.getDeploymentType());
         }
         possibleDeploymentTypes.retainAll(enabledDeploymentTypes);
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

   private static Set<Bean<?>> getMatchingBeans(AnnotatedItem<?, ?> element, List<Bean<?>> beans, MetaDataCache metaDataCache)
   {
      Set<Bean<?>> resolvedBeans = new HashSet<Bean<?>>();
      for (Bean<?> bean : beans)
      {
         if (element.isAssignableFrom(bean.getTypes()) && containsAllBindingBindingTypes(element, bean.getBindingTypes(), metaDataCache))
         {
            resolvedBeans.add(bean);
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
      StringBuffer buffer = new StringBuffer();
      buffer.append("Resolver\n");
      buffer.append(resolvedInjectionPoints.toString() + "\n");
      buffer.append("Injection points: " + injectionPoints.size() + "\n");
      int i = 0;
      for (AnnotatedItem<?, ?> injectionPoint : injectionPoints)
      {
         buffer.append(++i + " - " + injectionPoint.toString() + "\n");
      }
      buffer.append("Resolved names: " + resolvedNames.size() + "\n");
      i = 0;
      for (Entry<String, Set<Bean<?>>> entry : resolvedNames.entrySet())
      {
         buffer.append(++i + " - " + entry + ": " + entry.getValue().toString() + "\n");
      }
      return buffer.toString();
   }

}
