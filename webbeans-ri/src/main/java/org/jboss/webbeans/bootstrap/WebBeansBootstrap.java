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

import static org.jboss.webbeans.ejb.EJB.ENTERPRISE_BEAN_CLASS;
import static org.jboss.webbeans.jsf.JSF.UICOMPONENT_CLASS;
import static org.jboss.webbeans.servlet.Servlet.FILTER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.HTTP_SESSION_LISTENER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_CONTEXT_LISTENER_CLASS;
import static org.jboss.webbeans.servlet.Servlet.SERVLET_REQUEST_LISTENER_CLASS;
import static org.jboss.webbeans.util.BeanFactory.createEnterpriseBean;
import static org.jboss.webbeans.util.BeanFactory.createEventBean;
import static org.jboss.webbeans.util.BeanFactory.createInstanceBean;
import static org.jboss.webbeans.util.BeanFactory.createObserver;
import static org.jboss.webbeans.util.BeanFactory.createProducerFieldBean;
import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
import org.jboss.webbeans.MetaDataCache;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.InstanceBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bindings.InitializedBinding;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
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

   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   /**
    * Constructor
    * 
    * Starts up with the singleton Manager
    */
   public WebBeansBootstrap()
   {
      JNDI.set(ManagerImpl.JNDI_KEY, CurrentManager.rootManager());
   }

   /**
    * Register any beans defined by the provided classes with the manager
    * 
    * @param classes The classes to register
    */
   public void registerBeans(Class<?>... classes)
   {
      registerBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }

   /**
    * Register the bean with the manager
    * 
    * Creates the beans first and then sets them in the manager
    * 
    * @param classes The classes to register as Web Beans
    */
   public void registerBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = createBeans(classes);
      CurrentManager.rootManager().setBeans(beans);
   }

   /**
    * Discover any beans defined by the provided classes
    * 
    * Beans discovered are not registered with the manager
    * 
    * @param classes The classes to create Web Beans from
    * @return A set of Web Beans that represents the classes
    */
   public Set<AbstractBean<?, ?>> createBeans(Class<?>... classes)
   {
      return createBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
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
   public Set<AbstractBean<?, ?>> createBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      for (Class<?> clazz : classes)
      {
         if (MetaDataCache.instance().getEjbMetaData(clazz).isEjb())
         {
            createBean(createEnterpriseBean(clazz), beans);
         }
         else if (isTypeSimpleWebBean(clazz))
         {
            createBean(createSimpleBean(clazz), beans);
         }
      }
      return beans;
   }
   
   public void createBean(AbstractClassBean<?> bean, Set<AbstractBean<?, ?>> beans)
   {
      beans.add(bean);
      CurrentManager.rootManager().getResolver().addInjectionPoints(bean.getInjectionPoints());
      for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
      {
         ProducerMethodBean<?> producerMethodBean = createProducerMethodBean(producerMethod, bean);
         beans.add(producerMethodBean);
         CurrentManager.rootManager().getResolver().addInjectionPoints(producerMethodBean.getInjectionPoints());
         log.info("Web Bean: " + producerMethodBean);
      }
      for (AnnotatedField<Object> producerField : bean.getProducerFields())
      {
         ProducerFieldBean<?> producerFieldBean = createProducerFieldBean(producerField, bean);
         beans.add(producerFieldBean);
         log.info("Web Bean: " + producerFieldBean);
      }
      for (AnnotatedItem injectionPoint : bean.getInjectionPoints())
      {
         if ( injectionPoint.isAnnotationPresent(Observable.class) )  
         {
            EventBean<Object, Field> eventBean = createEventBean(injectionPoint);
            beans.add(eventBean);
            //CurrentManager.rootManager().getResolver().addInjectionPoints(eventBean.getInjectionPoints());
            log.info("Web Bean: " + eventBean);
         }
         if ( injectionPoint.isAnnotationPresent(Obtainable.class) )  
         {
            InstanceBean<Object, Field> instanceBean = createInstanceBean(injectionPoint);
            beans.add(instanceBean);
            //CurrentManager.rootManager().getResolver().addInjectionPoints(eventBean.getInjectionPoints());
            log.info("Web Bean: " + instanceBean);
         }
      }
      for (AnnotatedMethod<Object> observerMethod : bean.getObserverMethods())
      {
         ObserverImpl<?> observer = createObserver(observerMethod, bean);
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
   public void boot(WebBeanDiscovery webBeanDiscovery)
   {
      log.info("Starting Web Beans RI " + getVersion());
      if (webBeanDiscovery == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery provider found, you need to implement the org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery interface, and tell the RI to use it by specifying -D" + WebBeansBootstrap.WEB_BEAN_DISCOVERY_PROPERTY_NAME + "=<classname>");
      }
      registerBeans(webBeanDiscovery.discoverWebBeanClasses());
      log.info("Validing Web Bean injection points");
      CurrentManager.rootManager().getResolver().resolveInjectionPoints();
      CurrentManager.rootManager().fireEvent(CurrentManager.rootManager(), new InitializedBinding());
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
   
   @SuppressWarnings("unchecked")
   private static <T> void registerObserver(Observer<T> observer, Class<?> eventType, Annotation[] bindings)
   {
      CurrentManager.rootManager().addObserver(observer, (Class<T>) eventType, bindings);
   }
   
   protected static boolean isTypeSimpleWebBean(Class<?> type)
   {
      return !type.isAnnotation() && !Reflections.isAbstract(type) && !SERVLET_CLASS.isAssignableFrom(type) && !FILTER_CLASS.isAssignableFrom(type) && !SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(type) && !HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(type) && !SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(type) && !ENTERPRISE_BEAN_CLASS.isAssignableFrom(type) && !UICOMPONENT_CLASS.isAssignableFrom(type);
   }

}
