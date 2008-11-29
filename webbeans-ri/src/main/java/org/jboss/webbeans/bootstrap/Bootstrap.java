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

import static org.jboss.webbeans.util.BeanFactory.createEnterpriseBean;
import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

/**
 * Bootstrapping functionality that is run at application startup and detects
 * and register beans
 * 
 * @author Pete Muir
 */
public class Bootstrap
{
   // The property name of the discovery class
   public static String WEB_BEAN_DISCOVERY_PROPERTY_NAME = "org.jboss.webbeans.bootstrap.webBeanDiscovery";

   private static LogProvider log = Logging.getLogProvider(Bootstrap.class);

   // The Web Beans manager
   private ManagerImpl manager;

   /**
    * Constructor
    * 
    * Starts up with a fresh manager
    */
   public Bootstrap()
   {
      this(new ManagerImpl());
   }

   /**
    * Constructor
    * 
    * @param manager The Web Beans manager
    */
   protected Bootstrap(ManagerImpl manager)
   {
      this.manager = manager;
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
      manager.setBeans(beans);
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
         AbstractClassBean<?> bean;
         if (manager.getMetaDataCache().getEjbMetaData(clazz).isEjb())
         {
            bean = createEnterpriseBean(clazz, manager);
         }
         else
         {
            bean = createSimpleBean(clazz, manager);
         }
         beans.add(bean);
         manager.getResolver().addInjectionPoints(bean.getInjectionPoints());
         for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
         {
            ProducerMethodBean<?> producerMethodBean = createProducerMethodBean(producerMethod.getType(), producerMethod, manager, bean);
            beans.add(producerMethodBean);
            manager.getResolver().addInjectionPoints(producerMethodBean.getInjectionPoints());
         }
         log.info("Web Bean: " + bean);
      }
      return beans;
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
         throw new IllegalStateException("No WebBeanDiscovery provider found, you need to implement the org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery interface, and tell the RI to use it by specifying -D" + Bootstrap.WEB_BEAN_DISCOVERY_PROPERTY_NAME + "=<classname>");
      }
      registerBeans(webBeanDiscovery.discoverWebBeanClasses());
      log.info("Validing Web Bean injection points");
      manager.getResolver().resolveInjectionPoints();
   }

   /**
    * Gets version information
    * 
    * @return The implementation version from the Bootstrap class package.
    */
   public static String getVersion()
   {
      Package pkg = Bootstrap.class.getPackage();
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

}
