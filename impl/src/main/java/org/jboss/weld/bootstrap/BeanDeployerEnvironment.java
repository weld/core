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
package org.jboss.weld.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.New;

import org.jboss.weld.bean.AbstractBean;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.DisposalMethod;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.ManagedBean;
import org.jboss.weld.bean.NewBean;
import org.jboss.weld.bean.NewManagedBean;
import org.jboss.weld.bean.NewSessionBean;
import org.jboss.weld.bean.ProducerField;
import org.jboss.weld.bean.ProducerMethod;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resolution.TypeSafeDisposerResolver;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.AnnotatedTypes;

public class BeanDeployerEnvironment
{

   private final Map<WeldClass<?>, AbstractClassBean<?>> classBeanMap;
   private final Map<WeldMethodKey<?, ?>, ProducerMethod<?, ?>> producerMethodBeanMap;
   private final Set<RIBean<?>> beans;
   private final Set<ObserverMethodImpl<?, ?>> observers;
   private final List<DisposalMethod<?, ?>> allDisposalBeans;
   private final Set<DisposalMethod<?, ?>> resolvedDisposalBeans;
   private final Set<DecoratorImpl<?>> decorators;
   private final Set<InterceptorImpl<?>> interceptors;
   private final EjbDescriptors ejbDescriptors;
   private final TypeSafeDisposerResolver disposalMethodResolver;
   private final ClassTransformer classTransformer;
   private final Set<WeldClass<?>> newManagedBeanClasses;
   private final Set<InternalEjbDescriptor<?>> newSessionBeanDescriptors;

   public BeanDeployerEnvironment(EjbDescriptors ejbDescriptors, BeanManagerImpl manager)
   {
      this.classBeanMap = new HashMap<WeldClass<?>, AbstractClassBean<?>>();
      this.producerMethodBeanMap = new HashMap<WeldMethodKey<?, ?>, ProducerMethod<?, ?>>();
      this.allDisposalBeans = new ArrayList<DisposalMethod<?, ?>>();
      this.resolvedDisposalBeans = new HashSet<DisposalMethod<?, ?>>();
      this.beans = new HashSet<RIBean<?>>();
      this.decorators = new HashSet<DecoratorImpl<?>>();
      this.interceptors = new HashSet<InterceptorImpl<?>>();
      this.observers = new HashSet<ObserverMethodImpl<?, ?>>();
      this.ejbDescriptors = ejbDescriptors;
      this.disposalMethodResolver = new TypeSafeDisposerResolver(manager, allDisposalBeans);
      this.classTransformer = manager.getServices().get(ClassTransformer.class);
      this.newManagedBeanClasses = new HashSet<WeldClass<?>>();
      this.newSessionBeanDescriptors = new HashSet<InternalEjbDescriptor<?>>();
   }

   public Set<WeldClass<?>> getNewManagedBeanClasses()
   {
      return newManagedBeanClasses;
   }

   public Set<InternalEjbDescriptor<?>> getNewSessionBeanDescriptors()
   {
      return newSessionBeanDescriptors;
   }

   public <X, T> ProducerMethod<X, T> getProducerMethod(WeldMethod<X, T> method)
   {
      WeldMethodKey<X, T> key = new WeldMethodKey<X, T>(method);
      if (!producerMethodBeanMap.containsKey(key))
      {
         return null;
      }
      else
      {
         ProducerMethod<?, ?> bean = producerMethodBeanMap.get(key);
         bean.initialize(this);
         return (ProducerMethod<X, T>) bean;
      }
   }

   public AbstractClassBean<?> getClassBean(WeldClass<?> clazz)
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

   public void addProducerMethod(ProducerMethod<?, ?> bean)
   {
      producerMethodBeanMap.put(new WeldMethodKey(bean.getWeldAnnotated()), bean);
      addAbstractBean(bean);
   }

   public void addProducerField(ProducerField<?, ?> bean)
   {
      addAbstractBean(bean);
   }

   public void addExtension(ExtensionBean bean)
   {
      beans.add(bean);
   }

   public void addBuiltInBean(AbstractBuiltInBean<?> bean)
   {
      beans.add(bean);
   }

   protected void addAbstractClassBean(AbstractClassBean<?> bean)
   {
      if (!(bean instanceof NewBean))
      {
         classBeanMap.put(bean.getWeldAnnotated(), bean);
      }
      addAbstractBean(bean);
   }

   public void addManagedBean(ManagedBean<?> bean)
   {
      newManagedBeanClasses.add(bean.getWeldAnnotated());
      addAbstractClassBean(bean);
   }

   public void addSessionBean(SessionBean<?> bean)
   {
      newSessionBeanDescriptors.add(bean.getEjbDescriptor());
      addAbstractClassBean(bean);
   }

   public void addNewManagedBean(NewManagedBean<?> bean)
   {
      beans.add(bean);
   }

   public void addNewSessionBean(NewSessionBean<?> bean)
   {
      beans.add(bean);
   }

   protected void addAbstractBean(AbstractBean<?, ?> bean)
   {
      addNewBeansFromInjectionPoints(bean);
      beans.add(bean);
   }

   public void addDecorator(DecoratorImpl<?> bean)
   {
      decorators.add(bean);
   }

   public void addInterceptor(InterceptorImpl<?> bean)
   {
      interceptors.add(bean);
   }

   public void addDisposesMethod(DisposalMethod<?, ?> bean)
   {
      allDisposalBeans.add(bean);
      addNewBeansFromInjectionPoints(bean);
   }

   public void addObserverMethod(ObserverMethodImpl<?, ?> observer)
   {
      this.observers.add(observer);
      addNewBeansFromInjectionPoints(observer.getNewInjectionPoints());
   }

   private void addNewBeansFromInjectionPoints(AbstractBean<?, ?> bean)
   {
      addNewBeansFromInjectionPoints(bean.getNewInjectionPoints());
   }

   private void addNewBeansFromInjectionPoints(Set<WeldInjectionPoint<?, ?>> newInjectionPoints)
   {
      for (WeldInjectionPoint<?, ?> injectionPoint : newInjectionPoints)
      {
         // FIXME: better check
         if (injectionPoint.getJavaClass() == Instance.class || injectionPoint.getJavaClass() == Event.class)
         {
            continue;
         }
         New _new = injectionPoint.getAnnotation(New.class);
         if (_new.value().equals(New.class))
         {
            addNewBeanFromInjecitonPoint(injectionPoint.getJavaClass(), injectionPoint.getBaseType());
         }
         else
         {
            addNewBeanFromInjecitonPoint(_new.value(), _new.value());
         }

      }
   }

   private void addNewBeanFromInjecitonPoint(Class<?> rawType, Type baseType)
   {
      if (getEjbDescriptors().contains(rawType))
      {
         newSessionBeanDescriptors.add(getEjbDescriptors().getUnique(rawType));
      }
      else
      {
         newManagedBeanClasses.add(classTransformer.loadClass(rawType, baseType));
      }
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
    * @param qualifiers The binding types to match
    * @return The set of matching disposal methods
    */
   public <X> Set<DisposalMethod<X, ?>> resolveDisposalBeans(Set<Type> types, Set<Annotation> qualifiers, AbstractClassBean<X> declaringBean)
   {
      Set<DisposalMethod<X, ?>> beans = (Set) disposalMethodResolver.resolve(new ResolvableBuilder().addTypes(types).addQualifiers(qualifiers).setDeclaringBean(declaringBean).create());
      resolvedDisposalBeans.addAll(beans);
      return Collections.unmodifiableSet(beans);
   }

   private static class WeldMethodKey<T, X>
   {
      final WeldMethod meth;

      WeldMethodKey(WeldMethod<T, X> meth)
      {
         this.meth = meth;
      }

      @Override
      public boolean equals(Object other)
      {
         if (other instanceof WeldMethodKey<?, ?>)
         {
            WeldMethodKey<?, ?> o = (WeldMethodKey<?, ?>) other;
            return AnnotatedTypes.compareAnnotatedCallable(meth, o.meth);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return meth.getJavaMember().hashCode();
      }
   }

}
