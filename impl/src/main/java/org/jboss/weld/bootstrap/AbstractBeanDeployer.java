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
package org.jboss.weld.bootstrap;

import java.lang.reflect.Member;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AbstractClassBean;
import org.jboss.weld.bean.AbstractProducerBean;
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
import org.jboss.weld.bean.ee.EEResourceProducerField;
import org.jboss.weld.bean.ee.PersistenceContextProducerField;
import org.jboss.weld.bootstrap.events.ProcessBeanImpl;
import org.jboss.weld.bootstrap.events.ProcessBeanInjectionTarget;
import org.jboss.weld.bootstrap.events.ProcessManagedBeanImpl;
import org.jboss.weld.bootstrap.events.ProcessObserverMethodImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerFieldImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerImpl;
import org.jboss.weld.bootstrap.events.ProcessProducerMethodImpl;
import org.jboss.weld.bootstrap.events.ProcessSessionBeanImpl;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.event.ObserverFactory;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldField;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.jsf.JsfApiAbstraction;
import org.jboss.weld.log.LogProvider;
import org.jboss.weld.log.Logging;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.servlet.ServletApiAbstraction;
import org.jboss.weld.util.Reflections;
import org.jboss.weld.ws.WSApiAbstraction;

public class AbstractBeanDeployer<E extends BeanDeployerEnvironment>
{
   
   private static final LogProvider log = Logging.getLogProvider(AbstractBeanDeployer.class);
   
   private final BeanManagerImpl manager;
   private final E environment;
   
   public AbstractBeanDeployer(BeanManagerImpl manager, E environment)
   {
      this.manager = manager;
      this.environment = environment;
   }
   
   protected BeanManagerImpl getManager()
   {
      return manager;
   }
   
   public AbstractBeanDeployer<E> deploy()
   {
      Set<? extends RIBean<?>> beans = getEnvironment().getBeans();
      // ensure that all decorators are initialized before initializing 
      // the rest of the beans
      for (DecoratorImpl<?> bean : getEnvironment().getDecorators())
      {
         bean.initialize(getEnvironment());
         manager.addDecorator(bean);
         log.debug("Bean: " + bean);
      }
      for (InterceptorImpl<?> bean: getEnvironment().getInterceptors())
      {
         bean.initialize(getEnvironment());
         manager.addInterceptor(bean);
         log.debug("Interceptor: " + bean);
      }
      for (RIBean<?> bean : beans)
      {
         bean.initialize(getEnvironment());
         if (!(bean instanceof NewBean))
         {
            if (bean instanceof AbstractProducerBean<?, ?, ?>)
            {
               ProcessProducerImpl.fire(manager, (AbstractProducerBean<?, ?, Member>) bean);
            }
            else if (bean instanceof AbstractClassBean<?>)
            {
               ProcessBeanInjectionTarget.fire(manager, (AbstractClassBean<?>) bean);
            }
            if (bean instanceof ManagedBean<?>)
            {
               ProcessManagedBeanImpl.fire(manager, (ManagedBean<?>) bean);
            }
            else if (bean instanceof SessionBean<?>)
            {
               ProcessSessionBeanImpl.fire(manager, (SessionBean<Object>) bean);
            }
            else if (bean instanceof ProducerField<?, ?>)
            {
               ProcessProducerFieldImpl.fire(manager, (ProducerField<?, ?>) bean);
            }
            else if (bean instanceof ProducerMethod<?, ?>)
            {
               ProcessProducerMethodImpl.fire(manager, (ProducerMethod<?, ?>) bean);
            }
            else
            {
               ProcessBeanImpl.fire(getManager(), bean);
            }
         }
         manager.addBean(bean);
         log.debug("Bean: " + bean);
      }
      for (ObserverMethodImpl<?, ?> observer : getEnvironment().getObservers())
      {
         log.debug("Observer : " + observer);
         observer.initialize();
         ProcessObserverMethodImpl.fire(manager, observer);
         manager.addObserver(observer);
      }
      
      return this;
   }

   /**
    * Creates the sub bean for an class (simple or enterprise) bean
    * 
    * @param bean
    *           The class bean
    * 
    */
   protected <T> void createSubBeans(AbstractClassBean<T> bean)
   {
      createProducerMethods(bean, bean.getAnnotatedItem());
      createProducerFields(bean, bean.getAnnotatedItem());
      if (manager.isBeanEnabled(bean))
      {
         createObserverMethods(bean, bean.getAnnotatedItem());
      }
      createDisposalMethods(bean, bean.getAnnotatedItem());
      
   }
   
   protected <X> void createProducerMethods(AbstractClassBean<X> declaringBean, WeldClass<X> annotatedClass)
   {
      for (WeldMethod<?, X> method : annotatedClass.getDeclaredAnnotatedWeldMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);         
      }
   }
   
   protected <X> void createDisposalMethods(AbstractClassBean<X> declaringBean, WeldClass<X> annotatedClass)
   {
      for (WeldMethod<?, X> method : annotatedClass.getDeclaredWeldMethodsWithAnnotatedParameters(Disposes.class))
      {
         DisposalMethod<X, ?> disposalBean = DisposalMethod.of(manager, method, declaringBean);
         disposalBean.initialize(getEnvironment());
         getEnvironment().addBean(disposalBean);
      }
   }
   
   protected <X, T> void createProducerMethod(AbstractClassBean<X> declaringBean, WeldMethod<T, X> annotatedMethod)
   {
      ProducerMethod<X, T> bean = ProducerMethod.of(annotatedMethod, declaringBean, manager);
      getEnvironment().addBean(bean);
   }
   
   protected <X, T> void createProducerField(AbstractClassBean<X> declaringBean, WeldField<T, X> field)
   {
      ProducerField<X, T> bean;
      if (isPersistenceContextProducerField(field))
      {
         bean = PersistenceContextProducerField.of(field, declaringBean, manager);
      }
      else if (isEEResourceProducerField(field))
      {
         bean = EEResourceProducerField.of(field, declaringBean, manager);
      }
      else
      {
         bean = ProducerField.of(field, declaringBean, manager);
      }
      getEnvironment().addBean(bean);
   }
   
   protected <X> void createProducerFields(AbstractClassBean<X> declaringBean, WeldClass<X> annotatedClass)
   {
      for (WeldField<?, X> field : annotatedClass.getDeclaredAnnotatedWeldFields(Produces.class))
      {
         createProducerField(declaringBean, field);
      }
   }
   
   protected <X> void createObserverMethods(RIBean<X> declaringBean, WeldClass<X> annotatedClass)
   {
      for (WeldMethod<?, X> method : annotatedClass.getDeclaredWeldMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, method);
      }
   }
   
   protected <X, T> void createObserverMethod(RIBean<X> declaringBean, WeldMethod<T, X> method)
   {
      ObserverMethodImpl<X, T> observer = ObserverFactory.create(method, declaringBean, manager);
      getEnvironment().addObserver(observer);
   }

   protected <T> void createSimpleBean(WeldClass<T> annotatedClass)
   {
      ManagedBean<T> bean = ManagedBean.of(annotatedClass, manager);
      getEnvironment().addBean(bean);
      createSubBeans(bean);
      getEnvironment().addBean(NewManagedBean.of(annotatedClass, manager));
   }
   
   protected <T> void createDecorator(WeldClass<T> annotatedClass)
   {
      DecoratorImpl<T> bean = DecoratorImpl.of(annotatedClass, manager);
      getEnvironment().addBean(bean);
   }

   protected <T> void createInterceptor(WeldClass<T> annotatedClass)
   {
      InterceptorImpl<T> bean = InterceptorImpl.of(annotatedClass, manager);
      getEnvironment().addBean(bean);
   }
   
   protected <T> void createEnterpriseBean(InternalEjbDescriptor<T> ejbDescriptor)
   {
      // TODO Don't create enterprise bean if it has no local interfaces!
      SessionBean<T> bean = SessionBean.of(ejbDescriptor, manager);
      getEnvironment().addBean(bean);
      createSubBeans(bean);
      getEnvironment().addBean(NewSessionBean.of(ejbDescriptor, manager));
   }
   
   /**
    * Indicates if the type is a simple Web Bean
    * 
    * @param type
    *           The type to inspect
    * @return True if simple Web Bean, false otherwise
    */
   protected boolean isTypeManagedBeanOrDecorator(WeldClass<?> clazz)
   {
      Class<?> javaClass = clazz.getJavaClass();
      EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
      JsfApiAbstraction jsfApiAbstraction = manager.getServices().get(JsfApiAbstraction.class);
      ServletApiAbstraction servletApiAbstraction = manager.getServices().get(ServletApiAbstraction.class);
      return !clazz.isNonStaticMemberClass() &&
             !Reflections.isParamerterizedTypeWithWildcard(javaClass) && 
             !servletApiAbstraction.SERVLET_CLASS.isAssignableFrom(javaClass) && 
             !servletApiAbstraction.FILTER_CLASS.isAssignableFrom(javaClass) && 
             !servletApiAbstraction.SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(javaClass) && 
             !servletApiAbstraction.HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(javaClass) && 
             !servletApiAbstraction.SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(javaClass) && 
             !ejbApiAbstraction.ENTERPRISE_BEAN_CLASS.isAssignableFrom(javaClass) && 
             !jsfApiAbstraction.UICOMPONENT_CLASS.isAssignableFrom(javaClass) && 
             hasSimpleWebBeanConstructor(clazz);
   }
   
   protected boolean isEEResourceProducerField(WeldField<?, ?> field)
   {
      EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
      PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
      WSApiAbstraction wsApiAbstraction = manager.getServices().get(WSApiAbstraction.class);
      return field.isAnnotationPresent(ejbApiAbstraction.EJB_ANNOTATION_CLASS) || field.isAnnotationPresent(ejbApiAbstraction.RESOURCE_ANNOTATION_CLASS) || field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS) || field.isAnnotationPresent(wsApiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS); 
   }
   
   protected boolean isPersistenceContextProducerField(WeldField<?, ?> field)
   {
      PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
      return field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS); 
   }
   
   private static boolean hasSimpleWebBeanConstructor(WeldClass<?> type)
   {
      return type.getNoArgsWeldConstructor() != null || type.getAnnotatedWeldConstructors(Inject.class).size() > 0;
   }
      
   public E getEnvironment()
   {
      return environment;
   }
   
}
