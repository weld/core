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
package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.injection.resolution.ResolvableFactory;
import org.jboss.webbeans.injection.resolution.Resolver;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.WBMethod;

public class BeanDeployerEnvironment
{

   private final Map<WBClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<WBMethod<?>, ProducerMethodBean<?>> producerMethodBeanMap;
   private final Map<WBMethod<?>, DisposalMethodBean<?>> disposalMethodBeanMap;
   private final Set<RIBean<?>> beans;
   private final Set<ObserverImpl<?>> observers;
   private final List<DisposalMethodBean<?>> allDisposalBeans;
   private final Set<DisposalMethodBean<?>> resolvedDisposalBeans;
   private final EjbDescriptorCache ejbDescriptors;
   private final Resolver disposalMethodResolver;
   private final BeanManagerImpl manager;

   public BeanDeployerEnvironment(EjbDescriptorCache ejbDescriptors, BeanManagerImpl manager)
   {
      this.classBeanMap = new HashMap<WBClass<?>, AbstractClassBean<?>>();
      this.producerMethodBeanMap = new HashMap<WBMethod<?>, ProducerMethodBean<?>>();
      this.disposalMethodBeanMap = new HashMap<WBMethod<?>, DisposalMethodBean<?>>();
      this.allDisposalBeans = new ArrayList<DisposalMethodBean<?>>();
      this.resolvedDisposalBeans = new HashSet<DisposalMethodBean<?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.observers = new HashSet<ObserverImpl<?>>();
      this.ejbDescriptors = ejbDescriptors;
      this.disposalMethodResolver = new Resolver(manager, allDisposalBeans);
      this.manager = manager;
   }

   public ProducerMethodBean<?> getProducerMethod(WBMethod<?> method)
   {
      if (!producerMethodBeanMap.containsKey(method))
      {
         return null;
      }
      else
      {
         ProducerMethodBean<?> bean = producerMethodBeanMap.get(method);
         bean.initialize(this);
         return bean;
      }
   }

   
   public DisposalMethodBean<?> getDisposalMethod(WBMethod<?> method)
   {
      if (!producerMethodBeanMap.containsKey(method))
      {
         return null;
      }
      else
      {
         DisposalMethodBean<?> bean = disposalMethodBeanMap.get(method);
         bean.initialize(this);
         return bean;
      }
   }
   
   public AbstractClassBean<?> getClassBean(WBClass<?> clazz)
   {
      if (!classBeanMap.containsKey(clazz))
      {
         return null;
      }
      else
      {
         AbstractClassBean<?> bean = classBeanMap.get(clazz);
         bean.initialize(this);
         return bean;
      }
   }

   public void addBean(RIBean<?> value)
   {

      if (value instanceof AbstractClassBean && !(value instanceof NewBean))
      {
         AbstractClassBean<?> bean = (AbstractClassBean<?>) value;
         classBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      else if (value instanceof ProducerMethodBean)
      {
         ProducerMethodBean<?> bean = (ProducerMethodBean<?>) value;
         producerMethodBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      else if (value instanceof DisposalMethodBean)
      {
         DisposalMethodBean<?> bean = (DisposalMethodBean<?>) value;
         disposalMethodBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      beans.add(value);
   }

   public Set<RIBean<?>> getBeans()
   {
      return Collections.unmodifiableSet(beans);
   }

   public Set<ObserverImpl<?>> getObservers()
   {
      return observers;
   }

   public List<DisposalMethodBean<?>> getAllDisposalBeans()
   {
      return allDisposalBeans;
   }

   public void addDisposalBean(DisposalMethodBean<?> disposalBean)
   {
      allDisposalBeans.add(disposalBean);
   }

   public void addResolvedDisposalBean(DisposalMethodBean<?> disposalBean)
   {
      resolvedDisposalBeans.add(disposalBean);
   }

   public Set<DisposalMethodBean<?>> getResolvedDisposalBeans()
   {
      return resolvedDisposalBeans;
   }

   public EjbDescriptorCache getEjbDescriptors()
   {
      return ejbDescriptors;
   }
   
   /**
    * Resolve the disposal method for the given producer method. For internal
    * use.
    * 
    * @param apiType The API type to match
    * @param bindings The binding types to match
    * @return The set of matching disposal methods
    */
   public <T> Set<DisposalMethodBean<T>> resolveDisposalBeans(WBAnnotated<T, ?> annotatedItem)
   {
      // Correct?
      Set<Bean<?>> beans = disposalMethodResolver.get(ResolvableFactory.of(annotatedItem));
      Set<DisposalMethodBean<T>> disposalBeans = new HashSet<DisposalMethodBean<T>>();
      for (Bean<?> bean : beans)
      {
         if (bean instanceof DisposalMethodBean)
         {
            disposalBeans.add((DisposalMethodBean<T>) bean);
         }
      }
      return disposalBeans;
   }

}
