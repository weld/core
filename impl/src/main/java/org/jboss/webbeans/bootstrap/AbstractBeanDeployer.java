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

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Produces;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.DecoratorBean;
import org.jboss.webbeans.bean.DisposalMethodBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.event.ObserverFactory;
import org.jboss.webbeans.event.ObserverImpl;
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
   
   public AbstractBeanDeployer(BeanManagerImpl manager, BeanDeployerEnvironment environment)
   {
      this.manager = manager;
      this.environment = environment;
   }
   
   protected BeanManagerImpl getManager()
   {
      return manager;
   }
   
   public <T> AbstractBeanDeployer addBean(RIBean<T> bean)
   {
      this.environment.addBean(bean);
      return this;
   }
   
   public AbstractBeanDeployer deploy()
   {
      Set<RIBean<?>> beans = environment.getBeans();
      // ensure that all disposal methods are initialized before initializing 
      // the rest of the beans
      for (DecoratorBean<?> bean : environment.getDecorators())
      {
         bean.initialize(environment);
         manager.addRIBean(bean);
         log.debug("Bean: " + bean);
      }
      for (RIBean<?> bean : beans)
      {
         bean.initialize(environment);
         manager.addRIBean(bean);
         log.debug("Bean: " + bean);
      }
      for (ObserverImpl<?> observer : environment.getObservers())
      {
         observer.initialize();
         log.debug("Observer : " + observer);
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
      createObserverMethods(bean, bean.getAnnotatedItem());
      createDisposalMethods(bean, bean.getAnnotatedItem());
      
   }
   
   protected void createProducerMethods(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBMethod<?> method : annotatedClass.getDeclaredAnnotatedMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);         
      }
   }
   
   protected void createDisposalMethods(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBMethod<?> method : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Disposes.class))
      {
         DisposalMethodBean<?> disposalBean = DisposalMethodBean.of(manager, method, declaringBean);
         environment.addDisposalBean(disposalBean);
      }
   }
   
   protected <T> void createProducerMethod(AbstractClassBean<?> declaringBean, WBMethod<T> annotatedMethod)
   {
      ProducerMethodBean<T> bean = ProducerMethodBean.of(annotatedMethod, declaringBean, manager);
      addBean(bean);
   }
   
   protected <T> void createProducerField(AbstractClassBean<?> declaringBean, WBField<T> field)
   {
      ProducerFieldBean<T> bean = ProducerFieldBean.of(field, declaringBean, manager);
      addBean(bean);
   }
   
   protected void createProducerFields(AbstractClassBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBField<?> field : annotatedClass.getDeclaredAnnotatedFields(Produces.class))
      {
         createProducerField(declaringBean, field);
      }
   }
   
   protected void createObserverMethods(RIBean<?> declaringBean, WBClass<?> annotatedClass)
   {
      for (WBMethod<?> method : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, method);
      }
   }
   
   protected void createObserverMethod(RIBean<?> declaringBean, WBMethod<?> method)
   {
      ObserverImpl<?> observer = ObserverFactory.create(method, declaringBean, manager);
      environment.getObservers().add(observer);
   }
   
   protected <T> void createSimpleBean(WBClass<T> annotatedClass)
   {
      SimpleBean<T> bean = SimpleBean.of(annotatedClass, manager);
      addBean(bean);
      createSubBeans(bean);
      addBean(NewSimpleBean.of(annotatedClass, manager));
   }
   
   protected <T> void createDecorator(WBClass<T> annotatedClass)
   {
      DecoratorBean<T> bean = DecoratorBean.of(annotatedClass, manager);
      addBean(bean);
   }
   
   protected <T> void createEnterpriseBean(WBClass<T> annotatedClass)
   {
      // TODO Don't create enterprise bean if it has no local interfaces!
      EnterpriseBean<T> bean = EnterpriseBean.of(annotatedClass, manager, environment);
      addBean(bean);
      createSubBeans(bean);
      addBean(NewEnterpriseBean.of(annotatedClass, manager, environment));
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
      Class<?> rawType = clazz.getJavaClass();
      EJBApiAbstraction ejbApiAbstraction = manager.getServices().get(EJBApiAbstraction.class);
      JsfApiAbstraction jsfApiAbstraction = manager.getServices().get(JsfApiAbstraction.class);
      ServletApiAbstraction servletApiAbstraction = manager.getServices().get(ServletApiAbstraction.class);
      return !clazz.isNonStaticMemberClass() &&
             !Reflections.isParameterizedType(rawType) && 
             !servletApiAbstraction.SERVLET_CLASS.isAssignableFrom(rawType) && 
             !servletApiAbstraction.FILTER_CLASS.isAssignableFrom(rawType) && 
             !servletApiAbstraction.SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(rawType) && 
             !servletApiAbstraction.HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(rawType) && 
             !servletApiAbstraction.SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(rawType) && 
             !ejbApiAbstraction.ENTERPRISE_BEAN_CLASS.isAssignableFrom(rawType) && 
             !jsfApiAbstraction.UICOMPONENT_CLASS.isAssignableFrom(rawType) && 
             hasSimpleWebBeanConstructor(clazz);
   }
   
   private static boolean hasSimpleWebBeanConstructor(WBClass<?> type)
   {
      return type.getNoArgsConstructor() != null || type.getAnnotatedConstructors(Initializer.class).size() > 0;
   }
      
   public BeanDeployerEnvironment getBeanDeployerEnvironment()
   {
      return environment;
   }
   
}
