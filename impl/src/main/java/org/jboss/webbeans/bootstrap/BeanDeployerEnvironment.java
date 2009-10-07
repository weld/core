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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DecoratorImpl;
import org.jboss.webbeans.bean.DisposalMethod;
import org.jboss.webbeans.bean.NewBean;
import org.jboss.webbeans.bean.ProducerField;
import org.jboss.webbeans.bean.ProducerMethod;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.InterceptorImpl;
import org.jboss.webbeans.bean.builtin.AbstractBuiltInBean;
import org.jboss.webbeans.bean.builtin.ExtensionBean;
import org.jboss.webbeans.ejb.EjbDescriptors;
import org.jboss.webbeans.event.ObserverMethodImpl;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.resolution.ResolvableFactory;
import org.jboss.webbeans.resolution.TypeSafeDisposerResolver;

public class BeanDeployerEnvironment
{

   private final Map<WBClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<WBMethod<?, ?>, ProducerMethod<?, ?>> producerMethodBeanMap;
   private final Set<RIBean<?>> beans;
   private final Set<ObserverMethodImpl<?, ?>> observers;
   private final List<DisposalMethod<?, ?>> allDisposalBeans;
   private final Set<DisposalMethod<?, ?>> resolvedDisposalBeans;
   private final Set<DecoratorImpl<?>> decorators;
   private final Set<InterceptorImpl<?>> interceptors;
   private final EjbDescriptors ejbDescriptors;
   private final TypeSafeDisposerResolver disposalMethodResolver;

   public BeanDeployerEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager)
   {
      this.classBeanMap = new HashMap<WBClass<?>, AbstractClassBean<?>>();
      this.producerMethodBeanMap = new HashMap<WBMethod<?, ?>, ProducerMethod<?, ?>>();
      this.allDisposalBeans = new ArrayList<DisposalMethod<?, ?>>();
      this.resolvedDisposalBeans = new HashSet<DisposalMethod<?, ?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.decorators = new HashSet<DecoratorImpl<?>>();
      this.interceptors = new HashSet<InterceptorImpl<?>>();
      this.observers = new HashSet<ObserverMethodImpl<?, ?>>();
      this.ejbDescriptors = ejbDescriptors;
      this.disposalMethodResolver = new TypeSafeDisposerResolver(manager, allDisposalBeans);
   }

   public <X, T> ProducerMethod<X, T> getProducerMethod(WBMethod<X, T> method)
   {
      if (!producerMethodBeanMap.containsKey(method))
      {
         return null;
      }
      else
      {
         ProducerMethod<?, ?> bean = producerMethodBeanMap.get(method);
         bean.initialize(this);
         return (ProducerMethod<X, T>) bean;
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

   public void addBean(ProducerMethod<?, ?> bean)
   {
      producerMethodBeanMap.put(bean.getAnnotatedItem(), bean);
      beans.add(bean);
   }
   
   public void addBean(ProducerField<?, ?> bean)
   {
      beans.add(bean);
   }
   
   public void addBean(ExtensionBean bean)
   {
      beans.add(bean);
   }
   
   public void addBean(AbstractBuiltInBean<?> bean)
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

   public void addBean(DecoratorImpl<?> bean)
   {
      decorators.add(bean);
   }

   public void addBean(InterceptorImpl<?> bean)
   {
      interceptors.add(bean);
   }
   
   public void addBean(DisposalMethod<?, ?> bean)
   {
      allDisposalBeans.add(bean);
   }
   
   public void addObserver(ObserverMethodImpl<?, ?> observer)
   {
      this.observers.add(observer);
   }

   public Set<? extends RIBean<?>> getBeans()
   {
      return Collections.unmodifiableSet(beans);
   }
   
   public Set<DecoratorImpl<?>> getDecorators()
   {
      return Collections.unmodifiableSet(decorators);
   }

   public Set<InterceptorImpl<?>> getInterceptors()
   {
      return Collections.unmodifiableSet(interceptors);
   }

   public Set<ObserverMethodImpl<?, ?>> getObservers()
   {
      return Collections.unmodifiableSet(observers);
   }


   public Set<DisposalMethod<?, ?>> getUnresolvedDisposalBeans()
   {
      Set<DisposalMethod<?, ?>> beans = new HashSet<DisposalMethod<?, ?>>(allDisposalBeans);
      beans.removeAll(resolvedDisposalBeans);
      return Collections.unmodifiableSet(beans);
   }

   public EjbDescriptors getEjbDescriptors()
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
   public <X> Set<DisposalMethod<X, ?>> resolveDisposalBeans(Set<Type> types, Set<Annotation> bindings, AbstractClassBean<X> declaringBean)
   {
      Set<DisposalMethod<X, ?>> beans = (Set) disposalMethodResolver.resolve(ResolvableFactory.of(types, bindings, declaringBean));
      resolvedDisposalBeans.addAll(beans);
      return Collections.unmodifiableSet(beans);
   }

}
