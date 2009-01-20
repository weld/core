package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.BindingType;
import javax.webbeans.Fires;
import javax.webbeans.Initializer;
import javax.webbeans.Observes;
import javax.webbeans.Obtains;
import javax.webbeans.Produces;
import javax.webbeans.Realizes;

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
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.jsf.JSFApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.util.Reflections;

public class BeanDeployer
{
   
   private static final LogProvider log = Logging.getLogProvider(BeanDeployer.class);
   
   private static final Set<Annotation> EMPTY_BINDINGS = Collections.emptySet();
   
   private final Set<AbstractBean<?, ?>> beans;
   private final ManagerImpl manager;
   
   public BeanDeployer(ManagerImpl manager)
   {
      this.beans = new HashSet<AbstractBean<?,?>>();
      this.manager = manager;
   }
   
   public void addBean(AbstractBean<?, ?> bean)
   {
      this.beans.add(bean);
   }
   
   public void addBeans(Set<AbstractBean<?, ?>> beans)
   {
      this.beans.addAll(beans);
   }
   
   public void addClass(Class<?> clazz)
   {
      AnnotatedClass<?> annotatedClass = AnnotatedClassImpl.of(clazz);
      if (manager.getEjbDescriptorCache().containsKey(clazz))
      {
         createBean(EnterpriseBean.of(annotatedClass, manager), annotatedClass);
         beans.add(NewEnterpriseBean.of(annotatedClass, manager));
      }
      else if (isTypeSimpleWebBean(clazz))
      {
         createBean(SimpleBean.of(annotatedClass, manager), annotatedClass);
         beans.add(NewSimpleBean.of(annotatedClass, manager));
      }
   }
   
   public void addClasses(Iterable<Class<?>> classes)
   {
      for (Class<?> clazz : classes)
      {
         addClass(clazz);
      }
   }
   
   public void deploy()
   {
      manager.setBeans(beans);
   }
   
   /**
    * Creates a Web Bean from a bean abstraction and adds it to the set of
    * created beans
    * 
    * Also creates the implicit field- and method-level beans, if present
    * 
    * @param bean The bean representation
    */
   protected void createBean(AbstractClassBean<?> bean, final AnnotatedClass<?> annotatedClass)
   {
      
      beans.add(bean);
      
      manager.getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
      
      registerProducerMethods(bean, annotatedClass, EMPTY_BINDINGS);
      registerProducerFields(bean, annotatedClass, EMPTY_BINDINGS);
      registerObserverMethods(bean, annotatedClass);
      registerFacades(bean.getAnnotatedInjectionPoints());
      
      if (annotatedClass.isAnnotationPresent(Realizes.class))
      {
         Set<Annotation> extraAnnotations = new HashSet<Annotation>();
         extraAnnotations.addAll(annotatedClass.getDeclaredMetaAnnotations(BindingType.class));
         registerProducerMethods(bean, annotatedClass.getSuperclass(), extraAnnotations);
         registerProducerFields(bean, annotatedClass.getSuperclass(), extraAnnotations);
      }
      
      log.info("Web Bean: " + bean);
   }
   
   private void registerProducerMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass, Set<Annotation> extraAnnotations)
   {
      for (AnnotatedMethod<?> method : annotatedClass.getDeclaredAnnotatedMethods(Produces.class))
      {
         ProducerMethodBean<?> bean = ProducerMethodBean.of(method.wrap(extraAnnotations), declaringBean, manager);
         beans.add(bean);
         manager.getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
         registerFacades(bean.getAnnotatedInjectionPoints());
         log.info("Web Bean: " + bean);
      }
   }
   
   private void registerProducerFields(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass, Set<Annotation> extraAnnotations)
   {
      for (AnnotatedField<?> field : annotatedClass.getDeclaredAnnotatedFields(Produces.class))
      {
         ProducerFieldBean<?> bean = ProducerFieldBean.of(field.wrap(extraAnnotations), declaringBean, manager);
         beans.add(bean);
         log.info("Web Bean: " + bean);
      }
   }

   private void registerObserverMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedMethod<?> observerMethod : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Observes.class))
      {
         ObserverImpl<?> observer = ObserverImpl.of(observerMethod, declaringBean, manager);
         manager.addObserver(observer);
      }
   }

   private void registerFacades(Set<AnnotatedItem<?, ?>> injectionPoints)
   {
      for (AnnotatedItem<?, ?> injectionPoint : injectionPoints)
      {
         if (injectionPoint.isAnnotationPresent(Fires.class))
         {
             registerEvent(injectionPoint);
         }
         if (injectionPoint.isAnnotationPresent(Obtains.class))
         {
            registerInstance(injectionPoint);
         }
      }
   }

   private void registerEvent(AnnotatedItem<?, ?> injectionPoint)
   {
      // TODO Fix this!
      @SuppressWarnings("unchecked")
      EventBean<Object, Method> bean = EventBean.of((AnnotatedItem) injectionPoint, manager);
      beans.add(bean);
      log.info("Web Bean: " + bean);
   }
   
   private void registerInstance(AnnotatedItem<?, ?> injectionPoint)
   {
      // TODO FIx this
      @SuppressWarnings("unchecked")
      InstanceBean<Object, Field> bean = InstanceBean.of((AnnotatedItem) injectionPoint, manager);
      beans.add(bean);
      log.info("Web Bean: " + bean);
   }
   
   /**
    * Indicates if the type is a simple Web Bean
    * 
    * @param type The type to inspect
    * @return True if simple Web Bean, false otherwise
    */
   private boolean isTypeSimpleWebBean(Class<?> type)
   {
      EJBApiAbstraction ejbApiAbstraction = new EJBApiAbstraction(manager.getResourceLoader());
      JSFApiAbstraction jsfApiAbstraction = new JSFApiAbstraction(manager.getResourceLoader());
      ServletApiAbstraction servletApiAbstraction = new ServletApiAbstraction(manager.getResourceLoader());
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
