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

import static org.jboss.webbeans.bean.BeanFactory.createEnterpriseBean;
import static org.jboss.webbeans.bean.BeanFactory.createEventBean;
import static org.jboss.webbeans.bean.BeanFactory.createInstanceBean;
import static org.jboss.webbeans.bean.BeanFactory.createObserver;
import static org.jboss.webbeans.bean.BeanFactory.createProducerFieldBean;
import static org.jboss.webbeans.bean.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.bean.BeanFactory.createSimpleBean;
import static org.jboss.webbeans.ejb.EJB.ENTERPRISE_BEAN_CLASS;
import static org.jboss.webbeans.jsf.JSF.UICOMPONENT_CLASS;
import static org.jboss.webbeans.servlet.Servlet.FILTER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.HTTP_SESSION_LISTENER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_CONTEXT_LISTENER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_REQUEST_LISTENER_CLASS;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Observable;
import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Obtainable;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.InstanceBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bindings.InitializedBinding;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.contexts.DependentContext;
import org.jboss.webbeans.ejb.DefaultEnterpriseBeanLookup;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.transaction.Transaction;
import org.jboss.webbeans.util.JNDI;
import org.jboss.webbeans.util.Reflections;

/**
 * Bootstrapping functionality that is run at application startup and detects
 * and register beans
 * 
 * @author Pete Muir
 */
public class WebBeansBootstrap
{
   // The property name of the discovery class
   public static String WEB_BEAN_DISCOVERY_PROPERTY_NAME = "org.jboss.webbeans.bootstrap.webBeanDiscovery";
   // The log provider
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);
   // The Web Beans manager
   protected ManagerImpl manager;

   /**
    * Constructor
    * 
    * Starts up with the singleton Manager
    */
   public WebBeansBootstrap(ManagerImpl manager)
   {
      this.manager = manager;
      registerManager();
      manager.addContext(DependentContext.INSTANCE);
   }
   
   protected void registerManager()
   {
      JNDI.bind(ManagerImpl.JNDI_KEY, manager);
      CurrentManager.setRootManager(manager);
   }
   
   public WebBeansBootstrap()
   {
      this(new ManagerImpl());
   }

   /**
    * Register any beans defined by the provided classes with the manager
    * 
    * @param classes The classes to register
    */
   protected void registerBeans(Class<?>... classes)
   {
      registerBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }

   /**
    * Register the bean with the manager, including any standard (built in) beans
    * 
    * @param classes The classes to register as Web Beans
    */
   protected void registerBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = createBeans(classes);
      beans.addAll(createStandardBeans());
      manager.setBeans(beans);
   }
   
   /**
    * Creates the standard beans used internally by the RI
    * 
    * @return A set containing the created beans
    */
   protected Set<AbstractBean<?, ?>> createStandardBeans()
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      createBean(BeanFactory.createSimpleBean(Transaction.class, manager), beans);
      createBean(BeanFactory.createSimpleBean(ManagerImpl.class, manager), beans);
      createBean(BeanFactory.createSimpleBean(DefaultEnterpriseBeanLookup.class, manager), beans);
      return beans;
   }

   /**
    * Creates Web Beans from a set of classes
    * 
    * Iterates over the classes and creates a Web Bean of the corresponding
    * type. Also register the beans injection points with the resolver. If the
    * bean has producer methods, producer beans are created for these and those
    * injection points are also registered.
    * 
    * @param classes The classes to adapt
    * @return A set of adapted Web Beans
    */
   protected Set<AbstractBean<?, ?>> createBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      for (Class<?> clazz : classes)
      {
         if (manager.getEjbDescriptorCache().containsKey(clazz))
         {
            createBean(createEnterpriseBean(clazz, manager), beans);
         }
         else if (isTypeSimpleWebBean(clazz))
         {
            createBean(createSimpleBean(clazz, manager), beans);
         }
      }
      return beans;
   }
   
   /**
    * Creates a Web Bean from a bean abstraction and adds it to the set of created beans
    * 
    * Also creates the implicit field- and method-level beans, if present
    * 
    * @param bean The bean representation
    * @param beans The set of created beans
    */
   @SuppressWarnings("unchecked")
   protected void createBean(AbstractClassBean<?> bean, Set<AbstractBean<?, ?>> beans)
   {
      beans.add(bean);
      manager.getResolver().addInjectionPoints(bean.getInjectionPoints());
      for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
      {
         ProducerMethodBean<?> producerMethodBean = createProducerMethodBean(producerMethod, bean, manager);
         beans.add(producerMethodBean);
         manager.getResolver().addInjectionPoints(producerMethodBean.getInjectionPoints());
         registerEvents(producerMethodBean.getInjectionPoints(), beans);
         log.info("Web Bean: " + producerMethodBean);
      }
      for (AnnotatedField<Object> producerField : bean.getProducerFields())
      {
         ProducerFieldBean<?> producerFieldBean = createProducerFieldBean(producerField, bean, manager);
         beans.add(producerFieldBean);
         log.info("Web Bean: " + producerFieldBean);
      }
      for (AnnotatedMethod<Object> initializerMethod : bean.getInitializerMethods())
      {
         for (AnnotatedParameter<Object> parameter : initializerMethod.getAnnotatedParameters(Observable.class))
         {
            registerEvent(parameter, beans);
         }
      }
      for (AnnotatedItem injectionPoint : bean.getInjectionPoints())
      {
         if ( injectionPoint.isAnnotationPresent(Observable.class) )  
         {
            registerEvent(injectionPoint, beans);
         }
         if ( injectionPoint.isAnnotationPresent(Obtainable.class) )  
         {
            InstanceBean<Object, Field> instanceBean = createInstanceBean(injectionPoint, manager);
            beans.add(instanceBean);
            log.info("Web Bean: " + instanceBean);
         }
      }
      for (AnnotatedMethod<Object> observerMethod : bean.getObserverMethods())
      {
         ObserverImpl<?> observer = createObserver(observerMethod, bean, manager);
         if (observerMethod.getAnnotatedParameters(Observes.class).size() == 1)
         {
            registerObserver(observer, observerMethod.getAnnotatedParameters(Observes.class).get(0).getType(), observerMethod.getAnnotatedParameters(Observes.class).get(0).getBindingTypesAsArray());
         }
         else
         {
            throw new DefinitionException("Observer method can only have one parameter annotated @Observes " + observer);
         }
         
      }
      log.info("Web Bean: " + bean);
   }


   /**
    * Starts the boot process.
    * 
    * Discovers the beans and registers them with the manager. Also resolves the
    * injection points.
    * 
    * @param webBeanDiscovery The discovery implementation
    */
   public synchronized void boot(WebBeanDiscovery webBeanDiscovery)
   {
      log.info("Starting Web Beans RI " + getVersion());
      if (webBeanDiscovery == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery provider found, you need to implement the org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery interface, and tell the RI to use it by specifying -D" + WebBeansBootstrap.WEB_BEAN_DISCOVERY_PROPERTY_NAME + "=<classname>");
      }
      // Must populate EJB cache first, as we need it to detect whether a bean is an EJB!
      manager.getEjbDescriptorCache().addAll(webBeanDiscovery.discoverEjbs());
      registerBeans(webBeanDiscovery.discoverWebBeanClasses());
      log.info("Validing Web Bean injection points");
      manager.getResolver().resolveInjectionPoints();
      manager.fireEvent(manager, new InitializedBinding());
      log.info("Web Beans RI initialized");
   }

   /**
    * Gets version information
    * 
    * @return The implementation version from the Bootstrap class package.
    */
   public static String getVersion()
   {
      Package pkg = WebBeansBootstrap.class.getPackage();
      return pkg != null ? pkg.getImplementationVersion() : null;
   }

   /**
    * Gets the available discovery implementations
    * 
    * Parses the web-beans-ri.properties file and for each row describing a
    * discover class, instantiate that class and add it to the set
    * 
    * @return A set of discovery implementations
    * @see org.jboss.webbeans.bootstrap.DeploymentProperties
    */
   @SuppressWarnings("unchecked")
   public static Set<Class<? extends WebBeanDiscovery>> getWebBeanDiscoveryClasses()
   {
      Set<Class<? extends WebBeanDiscovery>> webBeanDiscoveryClasses = new HashSet<Class<? extends WebBeanDiscovery>>();
      for (String className : new DeploymentProperties(Thread.currentThread().getContextClassLoader()).getPropertyValues(WEB_BEAN_DISCOVERY_PROPERTY_NAME))
      {
         try
         {
            webBeanDiscoveryClasses.add((Class<WebBeanDiscovery>) Class.forName(className));
         }
         catch (ClassNotFoundException e)
         {
            log.debug("Unable to load WebBeanDiscovery provider " + className, e);
         }
         catch (NoClassDefFoundError e)
         {
            log.warn("Unable to load WebBeanDiscovery provider " + className + " due classDependencyProblem", e);
         }
      }
      return webBeanDiscoveryClasses;
   }

   /**
    * Registers an observer with the manager
    * 
    * @param observer The observer
    * @param eventType The event type to observe
    * @param bindings The binding types to observe on
    */
   @SuppressWarnings("unchecked")
   private <T> void registerObserver(Observer<T> observer, Class<?> eventType, Annotation[] bindings)
   {
      manager.addObserver(observer, (Class<T>) eventType, bindings);
   }
   
   /**
    * Iterates through the injection points and creates and registers any Event
    * observables specified with the @Observable annotation
    * 
    * @param injectionPoints A set of injection points to inspect
    * @param beans A set of beans to add the Event beans to
    */
   @SuppressWarnings("unchecked")
   private void registerEvents(Set<AnnotatedItem<?,?>> injectionPoints, Set<AbstractBean<?, ?>> beans)
   {
      for (AnnotatedItem injectionPoint : injectionPoints)
      {
         registerEvent(injectionPoint, beans);
      }
   }
   
   @SuppressWarnings("unchecked")
   private void registerEvent(AnnotatedItem injectionPoint, Set<AbstractBean<?, ?>> beans)
   {
      if ( injectionPoint.isAnnotationPresent(Observable.class) )
      {
         EventBean<Object, Method> eventBean = createEventBean(injectionPoint, manager);
         beans.add(eventBean);
         log.info("Web Bean: " + eventBean);
      }      
   }
   /**
    * Indicates if the type is a simple Web Bean
    * 
    * @param type The type to inspect
    * @return True if simple Web Bean, false otherwise
    */
   protected static boolean isTypeSimpleWebBean(Class<?> type)
   {
      return !type.isAnnotation() && !Reflections.isAbstract(type) && !SERVLET_CLASS.isAssignableFrom(type) && !FILTER_CLASS.isAssignableFrom(type) && !SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(type) && !HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(type) && !SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(type) && !ENTERPRISE_BEAN_CLASS.isAssignableFrom(type) && !UICOMPONENT_CLASS.isAssignableFrom(type);
   }

}
