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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Fires;
import javax.webbeans.Initializer;
import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Obtains;
import javax.webbeans.Produces;
import javax.webbeans.manager.Bean;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.InstanceBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.jsf.JSFApiAbstraction;
import org.jboss.webbeans.literal.DeployedLiteral;
import org.jboss.webbeans.literal.InitializedLiteral;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.transaction.Transaction;
import org.jboss.webbeans.util.BeanValidation;
import org.jboss.webbeans.util.Reflections;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public abstract class WebBeansBootstrap
{
  
   private static class ManagerBean extends SimpleBean<ManagerImpl>
   {
      
      public static final SimpleBean<ManagerImpl> of(ManagerImpl manager)
      {
         return new ManagerBean(AnnotatedClassImpl.of(ManagerImpl.class), manager);
      }
      
      protected ManagerBean(AnnotatedClass<ManagerImpl> type, ManagerImpl manager)
      {
         super(type, manager);
      }
      
      @Override
      protected void initConstructor()
      {
         // No - op, no constructor needed
      }

      @Override
      protected void initInjectionPoints()
      {
         annotatedInjectionPoints = Collections.emptySet();
      }

      @Override
      public ManagerImpl create()
      {
         return manager;
      }
      
   }
   
   // The log provider
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   // The Web Beans manager
   private ManagerImpl manager;

   protected void initManager(NamingContext namingContext, EjbResolver ejbResolver, ResourceLoader resourceLoader)
   {
      this.manager = new ManagerImpl(namingContext, ejbResolver, resourceLoader);
      manager.getNaming().bind(ManagerImpl.JNDI_KEY, getManager());
      CurrentManager.setRootManager(manager);
   }

   public ManagerImpl getManager()
   {
      return manager;
   }

   protected abstract WebBeanDiscovery getWebBeanDiscovery();
   
   protected abstract EjbDiscovery getEjbDiscovery();

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
    * Register the bean with the getManager(), including any standard (built in)
    * beans
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
      createSimpleBean(Transaction.class, beans);
      beans.add(ManagerBean.of(manager));
      return beans;
   }
   
   private <T> void createSimpleBean(Class<T> clazz, Set<AbstractBean<?, ?>> beans)
   {
      AnnotatedClass<T> annotatedClass = new AnnotatedClassImpl<T>(clazz);
      createBean(SimpleBean.of(annotatedClass, manager), annotatedClass, beans);
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
         AnnotatedClass<?> annotatedClass = AnnotatedClassImpl.of(clazz);
         if (getManager().getEjbDescriptorCache().containsKey(clazz))
         {
            createBean(EnterpriseBean.of(annotatedClass, getManager()), annotatedClass, beans);
            beans.add(NewEnterpriseBean.of(annotatedClass, manager));
         }
         else if (isTypeSimpleWebBean(clazz))
         {
            createBean(SimpleBean.of(annotatedClass, manager), annotatedClass, beans);
            beans.add(NewSimpleBean.of(annotatedClass, manager));
         }
      }
      return beans;
   }

   /**
    * Creates a Web Bean from a bean abstraction and adds it to the set of
    * created beans
    * 
    * Also creates the implicit field- and method-level beans, if present
    * 
    * @param bean The bean representation
    * @param beans The set of created beans
    */
   protected void createBean(AbstractClassBean<?> bean, AnnotatedClass<?> annotatedClass, Set<AbstractBean<?, ?>> beans)
   {
      beans.add(bean);
      getManager().getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
      for (AnnotatedMethod<?> producerMethod : annotatedClass.getAnnotatedMethods(Produces.class))
      {
         ProducerMethodBean<?> producerMethodBean = ProducerMethodBean.of(producerMethod, bean, getManager());
         beans.add(producerMethodBean);
         getManager().getResolver().addInjectionPoints(producerMethodBean.getAnnotatedInjectionPoints());
         registerEvents(producerMethodBean.getAnnotatedInjectionPoints(), beans);
         log.info("Web Bean: " + producerMethodBean);
      }
      for (AnnotatedField<?> producerField : annotatedClass.getAnnotatedFields(Produces.class))
      {
         ProducerFieldBean<?> producerFieldBean = ProducerFieldBean.of(producerField, bean, getManager());
         beans.add(producerFieldBean);
         log.info("Web Bean: " + producerFieldBean);
      }
      for (AnnotatedItem<?, ?> injectionPoint : bean.getAnnotatedInjectionPoints())
      {
         if (injectionPoint.isAnnotationPresent(Fires.class))
         {
            registerEvent(injectionPoint, beans);
         }
         if (injectionPoint.isAnnotationPresent(Obtains.class))
         {
            // TODO FIx this
            @SuppressWarnings("unchecked")
            InstanceBean<Object, Field> instanceBean = InstanceBean.of((AnnotatedItem) injectionPoint, getManager());
            beans.add(instanceBean);
            log.info("Web Bean: " + instanceBean);
         }
      }
      for (AnnotatedMethod<?> observerMethod : annotatedClass.getMethodsWithAnnotatedParameters(Observes.class))
      {
         ObserverImpl<?> observer = ObserverImpl.of(observerMethod, bean, getManager());
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
    * Discovers the beans and registers them with the getManager(). Also
    * resolves the injection points.
    * 
    * @param webBeanDiscovery The discovery implementation
    */
   public void boot()
   {
      synchronized (this)
      {
         log.info("Starting Web Beans RI " + getVersion());
         validateBootstrap();
         // Must populate EJB cache first, as we need it to detect whether a
         // bean is an EJB!
         getManager().getEjbDescriptorCache().addAll(getEjbDiscovery().discoverEjbs());
         registerBeans(getWebBeanDiscovery().discoverWebBeanClasses());
         getManager().fireEvent(getManager(), new InitializedLiteral());
         List<Bean<?>> beans = getManager().getBeans();
         log.info("Web Beans initialized. Validating beans.");
         getManager().getResolver().resolveInjectionPoints();
         BeanValidation.validate(getManager().getBeans());
         getManager().fireEvent(getManager(), new DeployedLiteral());
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
   private <T> void registerObserver(Observer<T> observer, Class<?> eventType, Annotation[] bindings)
   {
      // TODO Fix this!
      @SuppressWarnings("unchecked")
      Class<T> clazz = (Class<T>) eventType;
      getManager().addObserver(observer, clazz, bindings);
   }

   /**
    * Iterates through the injection points and creates and registers any Event
    * observables specified with the @Observable annotation
    * 
    * @param injectionPoints A set of injection points to inspect
    * @param beans A set of beans to add the Event beans to
    */
   private void registerEvents(Set<AnnotatedItem<?, ?>> injectionPoints, Set<AbstractBean<?, ?>> beans)
   {
      for (AnnotatedItem<?, ?> injectionPoint : injectionPoints)
      {
         registerEvent(injectionPoint, beans);
      }
   }

   private void registerEvent(AnnotatedItem<?, ?> injectionPoint, Set<AbstractBean<?, ?>> beans)
   {
      if (injectionPoint.isAnnotationPresent(Fires.class))
      {
         // TODO Fix this!
         @SuppressWarnings("unchecked")
         EventBean<Object, Method> eventBean = EventBean.of((AnnotatedItem) injectionPoint, getManager());
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
      // TODO: check 3.2.1 for more rules!!!!!!
      return !type.isAnnotation() && !Reflections.isAbstract(type) && !servletApiAbstraction.SERVLET_CLASS.isAssignableFrom(type) && !servletApiAbstraction.FILTER_CLASS.isAssignableFrom(type) && !servletApiAbstraction.SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(type) && !servletApiAbstraction.HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(type) && !servletApiAbstraction.SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(type) && !ejbApiAbstraction.ENTERPRISE_BEAN_CLASS.isAssignableFrom(type) && !jsfApiAbstraction.UICOMPONENT_CLASS.isAssignableFrom(type) && hasSimpleWebBeanConstructor(type);
   }

   private static boolean hasSimpleWebBeanConstructor(Class<?> type)
   {
      try
      {
         type.getDeclaredConstructor();
         return true;
      }
      catch (NoSuchMethodException nsme)
      {
         for (Constructor<?> c : type.getDeclaredConstructors())
         {
            if (c.isAnnotationPresent(Initializer.class))
               return true;
         }
         return false;
      }
   }

}
