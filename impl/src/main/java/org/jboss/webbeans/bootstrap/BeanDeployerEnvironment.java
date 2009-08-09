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

import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.standard.AbstractStandardBean;
import org.jboss.webbeans.bean.standard.ExtensionBean;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.introspector.WBAnnotated;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.resolution.ResolvableFactory;
import org.jboss.webbeans.resolution.TypeSafeBeanResolver;
import org.jboss.webbeans.resolution.TypeSafeResolver;

public class BeanDeployerEnvironment
{

   private final Map<WBClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<WBMethod<?, ?>, ProducerMethodBean<?>> producerMethodBeanMap;
   private final Map<WBMethod<?, ?>, DisposalMethodBean<?>> disposalMethodBeanMap;
   private final Set<RIBean<?>> beans;
   private final Set<ObserverMethod<?, ?>> observers;
   private final List<DisposalMethodBean<?>> allDisposalBeans;
   private final Set<DisposalMethodBean<?>> resolvedDisposalBeans;
   private final Set<DecoratorBean<?>> decorators;
   private final EjbDescriptorCache ejbDescriptors;
   private final TypeSafeResolver<DisposalMethodBean<?>> disposalMethodResolver;

   public BeanDeployerEnvironment(EjbDescriptorCache ejbDescriptors, BeanManagerImpl manager)
   {
      this.classBeanMap = new HashMap<WBClass<?>, AbstractClassBean<?>>();
      this.producerMethodBeanMap = new HashMap<WBMethod<?, ?>, ProducerMethodBean<?>>();
      this.disposalMethodBeanMap = new HashMap<WBMethod<?, ?>, DisposalMethodBean<?>>();
      this.allDisposalBeans = new ArrayList<DisposalMethodBean<?>>();
      this.resolvedDisposalBeans = new HashSet<DisposalMethodBean<?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.decorators = new HashSet<DecoratorBean<?>>();
      this.observers = new HashSet<ObserverMethod<?, ?>>();
      this.ejbDescriptors = ejbDescriptors;
      this.disposalMethodResolver = new TypeSafeBeanResolver<DisposalMethodBean<?>>(manager, allDisposalBeans);
   }

   public ProducerMethodBean<?> getProducerMethod(WBMethod<?, ?> method)
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

   
   public DisposalMethodBean<?> getDisposalMethod(WBMethod<?, ?> method)
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

   public void addBean(ProducerMethodBean<?> bean)
   {
      producerMethodBeanMap.put(bean.getAnnotatedItem(), bean);
      beans.add(bean);
   }
   
   public void addBean(ProducerFieldBean<?> bean)
   {
      beans.add(bean);
   }
   
   public void addBean(ExtensionBean bean)
   {
      beans.add(bean);
   }
   
   public void addBean(AbstractStandardBean<?> bean)
   {
      beans.add(bean);
   }
   
   public void addBean(AbstractClassBean<?> bean)
   {
      if (!(bean instanceof NewBean))
      {
         classBeanMap.put(bean.getAnnotatedItem(), bean);
      }
      beans.add(bean);
   }

   public void addBean(DecoratorBean<?> bean)
   {
      decorators.add(bean);
   }
   
   public void addBean(DisposalMethodBean<?> bean)
   {
      allDisposalBeans.add(bean);
      disposalMethodBeanMap.put(bean.getAnnotatedItem(), bean);
   }
   
   public void addObserver(ObserverMethod<?, ?> observer)
   {
      this.observers.add(observer);
   }

   public Set<RIBean<?>> getBeans()
   {
      return Collections.unmodifiableSet(beans);
   }
   
   public Set<DecoratorBean<?>> getDecorators()
   {
      return Collections.unmodifiableSet(decorators);
   }

   public Set<ObserverMethod<?, ?>> getObservers()
   {
      return Collections.unmodifiableSet(observers);
   }


   public Set<DisposalMethodBean<?>> getUnresolvedDisposalBeans()
   {
      Set<DisposalMethodBean<?>> beans = new HashSet<DisposalMethodBean<?>>(allDisposalBeans);
      beans.removeAll(resolvedDisposalBeans);
      return Collections.unmodifiableSet(beans);
   }

   public EjbDescriptorCache getEjbDescriptors()
   {
      return ejbDescriptors;
   }
   
   /**
    * Resolve the disposal method for the given producer method. Any resolved
    * beans will be marked as such for the purpose of validating that all
    * disposal methods are used. For internal use.
    * 
    * @param apiType The API type to match
    * @param bindings The binding types to match
    * @return The set of matching disposal methods
    */
   public <T> Set<DisposalMethodBean<T>> resolveDisposalBeans(WBAnnotated<T, ?> annotatedItem)
   {
      // Correct?
      Set<DisposalMethodBean<?>> beans = disposalMethodResolver.resolve(ResolvableFactory.of(annotatedItem));
      Set<DisposalMethodBean<T>> disposalBeans = new HashSet<DisposalMethodBean<T>>();
      for (DisposalMethodBean<?> bean : beans)
      {
         disposalBeans.add((DisposalMethodBean<T>) bean);
      }
      resolvedDisposalBeans.addAll(disposalBeans);
      return Collections.unmodifiableSet(disposalBeans);
   }

}
