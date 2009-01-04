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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Fires;
import javax.webbeans.Initializer;
import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Obtains;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.BeanFactory;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.InstanceBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.binding.InitializedBinding;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.AnnotatedParameter;
import org.jboss.webbeans.jsf.JSFApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.transaction.Transaction;
import org.jboss.webbeans.util.Reflections;

/**
 * Common bootstrapping functionality that is run at application startup and detects
 * and register beans
 * 
 * @author Pete Muir
 */
public abstract class WebBeansBootstrap
{ 
   // The log provider
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);
   
   protected void registerManager()
   {
      getManager().getNaming().bind(ManagerImpl.JNDI_KEY, getManager());
      CurrentManager.setRootManager(getManager());
   }
   
   public abstract ManagerImpl getManager();

   protected abstract WebBeanDiscovery getWebBeanDiscovery();

   public abstract ResourceLoader getResourceLoader();
   
   protected void validateBootstrap()
   {
      if (getManager() == null)
      {
         throw new IllegalStateException("getManager() is not set on bootstrap");
      }
      if (getWebBeanDiscovery() == null)
      {
         throw new IllegalStateException("WebBeanDiscovery plugin not set on bootstrap");
      }
      if (getResourceLoader() == null)
      {
         throw new IllegalStateException("ResourceLoader plugin not set on bootstrap");
      }
   }

   /**
    * Register any beans defined by the provided classes with the getManager()
    * 
    * @param classes The classes to register
    */
   protected void registerBeans(Class<?>... classes)
   {
      registerBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }

   /**
    * Register the bean with the getManager(), including any standard (built in) beans
    * 
    * @param classes The classes to register as Web Beans
    */
   protected void registerBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = createBeans(classes);
      beans.addAll(createStandardBeans());
      getManager().setBeans(beans);
   }
   
   /**
    * Creates the standard beans used internally by the RI
    * 
    * @return A set containing the created beans
    */
   protected Set<AbstractBean<?, ?>> createStandardBeans()
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      createBean(BeanFactory.createSimpleBean(Transaction.class, getManager()), beans);
      final ManagerImpl managerImpl = getManager();
      createBean(new SimpleBean<ManagerImpl>(ManagerImpl.class, getManager())
      {
   
         @Override
         public ManagerImpl create()
         {
            return managerImpl;
         }
   
      }, beans);
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
         if (getManager().getEjbDescriptorCache().containsKey(clazz))
         {
            createBean(createEnterpriseBean(clazz, getManager()), beans);
         }
         else if (isTypeSimpleWebBean(clazz))
         {
            createBean(createSimpleBean(clazz, getManager()), beans);
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
      getManager().getResolver().addInjectionPoints(bean.getInjectionPoints());
      for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
      {
         ProducerMethodBean<?> producerMethodBean = createProducerMethodBean(producerMethod, bean, getManager());
         beans.add(producerMethodBean);
         getManager().getResolver().addInjectionPoints(producerMethodBean.getInjectionPoints());
         registerEvents(producerMethodBean.getInjectionPoints(), beans);
         log.info("Web Bean: " + producerMethodBean);
      }
      for (AnnotatedField<Object> producerField : bean.getProducerFields())
      {
         ProducerFieldBean<?> producerFieldBean = createProducerFieldBean(producerField, bean, getManager());
         beans.add(producerFieldBean);
         log.info("Web Bean: " + producerFieldBean);
      }
      for (AnnotatedItem injectionPoint : bean.getInjectionPoints())
      {
         if ( injectionPoint.isAnnotationPresent(Fires.class) )  
         {
            registerEvent(injectionPoint, beans);
         }
         if ( injectionPoint.isAnnotationPresent(Obtains.class) )  
         {
            InstanceBean<Object, Field> instanceBean = createInstanceBean(injectionPoint, getManager());
            beans.add(instanceBean);
            log.info("Web Bean: " + instanceBean);
         }
      }
      for (AnnotatedMethod<Object> observerMethod : bean.getObserverMethods())
      {
         ObserverImpl<?> observer = createObserver(observerMethod, bean, getManager());
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
    * Discovers the beans and registers them with the getManager(). Also resolves the
    * injection points.
    * 
    * @param webBeanDiscovery The discovery implementation
    */
   public void boot()
   {
      synchronized (this)
      {
         log.info("Starting Web Beans RI " + getVersion());
         validateBootstrap();
         // Must populate EJB cache first, as we need it to detect whether a bean is an EJB!
         getManager().getEjbDescriptorCache().addAll(getWebBeanDiscovery().discoverEjbs());
         registerBeans(getWebBeanDiscovery().discoverWebBeanClasses());
         log.info("Validing Web Bean injection points");
         getManager().getResolver().resolveInjectionPoints();
         getManager().fireEvent(getManager(), new InitializedBinding());
         log.info("Web Beans RI initialized");
      }
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
    * Registers an observer with the getManager()
    * 
    * @param observer The observer
    * @param eventType The event type to observe
    * @param bindings The binding types to observe on
    */
   @SuppressWarnings("unchecked")
   private <T> void registerObserver(Observer<T> observer, Class<?> eventType, Annotation[] bindings)
   {
      getManager().addObserver(observer, (Class<T>) eventType, bindings);
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
      if ( injectionPoint.isAnnotationPresent(Fires.class) )
      {
         EventBean<Object, Method> eventBean = createEventBean(injectionPoint, getManager());
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
   protected boolean isTypeSimpleWebBean(Class<?> type)
   {
      EJBApiAbstraction ejbApiAbstraction = new EJBApiAbstraction(getResourceLoader());
      JSFApiAbstraction jsfApiAbstraction = new JSFApiAbstraction(getResourceLoader());
      ServletApiAbstraction servletApiAbstraction = new ServletApiAbstraction(getResourceLoader());
      //TODO: check 3.2.1 for more rules!!!!!!
      return !type.isAnnotation() && 
      	    !Reflections.isAbstract(type) && 
      	    !servletApiAbstraction.SERVLET_CLASS.isAssignableFrom(type) && 
      	    !servletApiAbstraction.FILTER_CLASS.isAssignableFrom(type) && 
      	    !servletApiAbstraction.SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(type) && 
      	    !servletApiAbstraction.HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(type) && 
      	    !servletApiAbstraction.SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(type) && 
      	    !ejbApiAbstraction.ENTERPRISE_BEAN_CLASS.isAssignableFrom(type) && 
      	    !jsfApiAbstraction.UICOMPONENT_CLASS.isAssignableFrom(type) &&
      	    hasSimpleWebBeanConstructor(type);
   }

   private static boolean hasSimpleWebBeanConstructor(Class<?> type) {
      try {
         type.getDeclaredConstructor();
         return true;
      }
      catch (NoSuchMethodException nsme)
      {
         for (Constructor<?> c: type.getDeclaredConstructors())
         {
            if (c.isAnnotationPresent(Initializer.class)) return true;
         }
         return false;
      }
   }

}
