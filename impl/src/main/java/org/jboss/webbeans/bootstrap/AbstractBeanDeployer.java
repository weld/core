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

import java.util.Set;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Interceptor;
import javax.inject.Inject;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.AbstractProducerBean;
import org.jboss.webbeans.bean.DecoratorImpl;
import org.jboss.webbeans.bean.DisposalMethod;
import org.jboss.webbeans.bean.ManagedBean;
import org.jboss.webbeans.bean.NewManagedBean;
import org.jboss.webbeans.bean.NewSessionBean;
import org.jboss.webbeans.bean.ProducerField;
import org.jboss.webbeans.bean.ProducerMethod;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SessionBean;
import org.jboss.webbeans.bean.InterceptorImpl;
import org.jboss.webbeans.bean.ee.EEResourceProducerField;
import org.jboss.webbeans.bean.ee.PersistenceContextProducerField;
import org.jboss.webbeans.bootstrap.events.ProcessObserverMethodImpl;
import org.jboss.webbeans.bootstrap.events.ProcessProducerImpl;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.event.ObserverFactory;
import org.jboss.webbeans.event.ObserverMethodImpl;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.jsf.JsfApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.util.Reflections;
import org.jboss.webbeans.util.reflection.ParameterizedTypeImpl;
import org.jboss.webbeans.ws.WSApiAbstraction;
import org.jboss.interceptor.model.InterceptionModel;

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
         if (bean instanceof AbstractProducerBean<?, ?, ?>)
         {
            AbstractProducerBean<?, ?, Member> producer = (AbstractProducerBean<?, ?, Member>) bean;
            createAndFireProcessProducerEvent(producer);
         }
         manager.addBean(bean);
         log.debug("Bean: " + bean);
      }
      for (ObserverMethodImpl<?, ?> observer : getEnvironment().getObservers())
      {
         log.debug("Observer : " + observer);
         observer.initialize();
         createAndFireProcessObserverMethodEvent(observer);
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
   
   protected <X> void createProducerMethods(AbstractClassBean<X> declaringBean, WBClass<X> annotatedClass)
   {
      for (WBMethod<?, X> method : annotatedClass.getDeclaredAnnotatedWBMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);         
      }
   }
   
   protected <X> void createDisposalMethods(AbstractClassBean<X> declaringBean, WBClass<X> annotatedClass)
   {
      for (WBMethod<?, X> method : annotatedClass.getDeclaredWBMethodsWithAnnotatedParameters(Disposes.class))
      {
         DisposalMethod<X, ?> disposalBean = DisposalMethod.of(manager, method, declaringBean);
         disposalBean.initialize(getEnvironment());
         getEnvironment().addBean(disposalBean);
      }
   }
   
   protected <X, T> void createProducerMethod(AbstractClassBean<X> declaringBean, WBMethod<T, X> annotatedMethod)
   {
      ProducerMethod<X, T> bean = ProducerMethod.of(annotatedMethod, declaringBean, manager);
      getEnvironment().addBean(bean);
   }
   
   protected <X, T> void createProducerField(AbstractClassBean<X> declaringBean, WBField<T, X> field)
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
   
   private <X, T> void createAndFireProcessProducerEvent(AbstractProducerBean<X, T, Member> producer)
   {
      ProcessProducerImpl<X, T> payload = new ProcessProducerImpl<X, T>(producer.getAnnotatedItem(), producer) {};
      fireEvent(payload, ProcessProducer.class, producer.getAnnotatedItem().getDeclaringType().getBaseType(), producer.getAnnotatedItem().getBaseType());
      if (!payload.getDefinitionErrors().isEmpty())
      {
         // FIXME communicate all the captured definition errors in this exception
         throw new DefinitionException(payload.getDefinitionErrors().get(0));
      }
   }
   
   private void fireEvent(Object payload, Type rawType, Type... actualTypeArguments)
   {
      Type eventType = new ParameterizedTypeImpl(rawType, actualTypeArguments, null);
      manager.fireEvent(eventType, payload);
   }
   
   protected <X> void createProducerFields(AbstractClassBean<X> declaringBean, WBClass<X> annotatedClass)
   {
      for (WBField<?, X> field : annotatedClass.getDeclaredAnnotatedWBFields(Produces.class))
      {
         createProducerField(declaringBean, field);
      }
   }
   
   protected <X> void createObserverMethods(RIBean<X> declaringBean, WBClass<X> annotatedClass)
   {
      for (WBMethod<?, X> method : annotatedClass.getDeclaredWBMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, method);
      }
   }
   
   protected <X, T> void createObserverMethod(RIBean<X> declaringBean, WBMethod<T, X> method)
   {
      ObserverMethodImpl<X, T> observer = ObserverFactory.create(method, declaringBean, manager);
      getEnvironment().addObserver(observer);
   }
   
   private <X, T> void createAndFireProcessObserverMethodEvent(ObserverMethodImpl<X, T> observer)
   {
      ProcessObserverMethodImpl<X, T> payload = new ProcessObserverMethodImpl<X, T>(observer.getMethod(), observer) {};
      fireEvent(payload, ProcessObserverMethod.class, observer.getMethod().getDeclaringType().getBaseType(), observer.getObservedType());
      if (!payload.getDefinitionErrors().isEmpty())
      {
         // FIXME communicate all the captured definition errors in this exception
         throw new DefinitionException(payload.getDefinitionErrors().get(0));
      }
      return;
   }

   protected <T> void createSimpleBean(WBClass<T> annotatedClass)
   {
      ManagedBean<T> bean = ManagedBean.of(annotatedClass, manager);
      getEnvironment().addBean(bean);
      createSubBeans(bean);
      getEnvironment().addBean(NewManagedBean.of(annotatedClass, manager));
   }
   
   protected <T> void createDecorator(WBClass<T> annotatedClass)
   {
      DecoratorImpl<T> bean = DecoratorImpl.of(annotatedClass, manager);
      getEnvironment().addBean(bean);
   }

   protected <T> void createInterceptor(WBClass<T> annotatedClass)
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
   protected boolean isTypeManagedBeanOrDecorator(WBClass<?> clazz)
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
   
   protected boolean isEEResourceProducerField(WBField<?, ?> field)
   {
      EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
      PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
      WSApiAbstraction wsApiAbstraction = manager.getServices().get(WSApiAbstraction.class);
      return field.isAnnotationPresent(ejbApiAbstraction.EJB_ANNOTATION_CLASS) || field.isAnnotationPresent(ejbApiAbstraction.RESOURCE_ANNOTATION_CLASS) || field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_UNIT_ANNOTATION_CLASS) || field.isAnnotationPresent(wsApiAbstraction.WEB_SERVICE_REF_ANNOTATION_CLASS); 
   }
   
   protected boolean isPersistenceContextProducerField(WBField<?, ?> field)
   {
      PersistenceApiAbstraction persistenceApiAbstraction = manager.getServices().get(PersistenceApiAbstraction.class);
      return field.isAnnotationPresent(persistenceApiAbstraction.PERSISTENCE_CONTEXT_ANNOTATION_CLASS); 
   }
   
   private static boolean hasSimpleWebBeanConstructor(WBClass<?> type)
   {
      return type.getNoArgsWBConstructor() != null || type.getAnnotatedWBConstructors(Inject.class).size() > 0;
   }
      
   public E getEnvironment()
   {
      return environment;
   }
   
}
