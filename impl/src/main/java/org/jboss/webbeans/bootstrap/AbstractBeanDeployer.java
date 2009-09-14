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
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.inject.Inject;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DecoratorImpl;
import org.jboss.webbeans.bean.DisposalMethod;
import org.jboss.webbeans.bean.SessionBean;
import org.jboss.webbeans.bean.NewSessionBean;
import org.jboss.webbeans.bean.NewManagedBean;
import org.jboss.webbeans.bean.ProducerField;
import org.jboss.webbeans.bean.ProducerMethod;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.ManagedBean;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.event.ObserverFactory;
import org.jboss.webbeans.event.ObserverMethodImpl;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.introspector.WBField;
import org.jboss.webbeans.introspector.WBMethod;
import org.jboss.webbeans.jsf.JsfApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.util.Reflections;

public class AbstractBeanDeployer
{
   
   private static final LogProvider log = Logging.getLogProvider(AbstractBeanDeployer.class);
   
   private final BeanManagerImpl manager;
   private final BeanDeployerEnvironment environment;
   private final List<Throwable> definitionErrors = new ArrayList<Throwable>();
   
   public AbstractBeanDeployer(BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      this.manager = manager;
      this.environment = environment;
   }
   
   protected BeanManagerImpl getManager()
   {
      return manager;
   }
   
   public AbstractBeanDeployer deploy()
   {
      Set<RIBean<?>> beans = getEnvironment().getBeans();
      // ensure that all decorators are initialized before initializing 
      // the rest of the beans
      for (DecoratorImpl<?> bean : getEnvironment().getDecorators())
      {
         bean.initialize(getEnvironment());
         manager.addDecorator(bean);
         log.debug("Bean: " + bean);
      }
      for (RIBean<?> bean : beans)
      {
         bean.initialize(getEnvironment());
         manager.addBean(bean);
         log.debug("Bean: " + bean);
      }
      for (ObserverMethodImpl<?, ?> observer : getEnvironment().getObservers())
      {
         log.debug("Observer : " + observer);
         if (observer instanceof ObserverMethodImpl<?, ?>)
         {
            observer.initialize();
         }
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
   
   protected void createProducerMethods(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBMethod<?, ?> method : annotatedClass.getDeclaredWBAnnotatedMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);         
      }
   }
   
   protected void createDisposalMethods(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBMethod<?, ?> method : annotatedClass.getWBDeclaredMethodsWithAnnotatedParameters(Disposes.class))
      {
         DisposalMethod<?> disposalBean = DisposalMethod.of(manager, method, declaringBean);
         disposalBean.initialize(getEnvironment());
         getEnvironment().addBean(disposalBean);
      }
   }
   
   protected <T> void createProducerMethod(AbstractClassBean<?> declaringBean, WBMethod<T, ?> annotatedMethod)
   {
      ProducerMethod<T> bean = ProducerMethod.of(annotatedMethod, declaringBean, manager);
      getEnvironment().addBean(bean);
   }
   
   protected <T> void createProducerField(AbstractClassBean<?> declaringBean, WBField<T, ?> field)
   {
      ProducerField<T> bean = ProducerField.of(field, declaringBean, manager);
      getEnvironment().addBean(bean);
   }
   
   protected void createProducerFields(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBField<?, ?> field : annotatedClass.getDeclaredAnnotatedWBFields(Produces.class))
      {
         createProducerField(declaringBean, field);
      }
   }
   
   protected <X> void createObserverMethods(RIBean<X> declaringBean, WBClass<X> annotatedClass)
   {
      for (WBMethod<?, X> method : annotatedClass.getWBDeclaredMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, method);
      }
   }
   
   protected <X, T> void createObserverMethod(RIBean<X> declaringBean, WBMethod<T, X> method)
   {
      ObserverMethodImpl<X, T> observer = ObserverFactory.create(method, declaringBean, manager);
      ProcessObserverMethod<?, ?> event = createProcessObserverMethodEvent(observer, method);
      manager.fireEvent(event);
      getEnvironment().addObserver(observer);
   }
   
   private <X, T> ProcessObserverMethod<X, T> createProcessObserverMethodEvent(ObserverMethod<X, T> observer, AnnotatedMethod<X> method)
   {
      return new ProcessObserverMethodImpl<X, T>(method, observer, definitionErrors) {};
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
   
   private static boolean hasSimpleWebBeanConstructor(WBClass<?> type)
   {
      return type.getNoArgsWBConstructor() != null || type.getAnnotatedWBConstructors(Inject.class).size() > 0;
   }
      
   public BeanDeployerEnvironment getEnvironment()
   {
      return environment;
   }
   
}
