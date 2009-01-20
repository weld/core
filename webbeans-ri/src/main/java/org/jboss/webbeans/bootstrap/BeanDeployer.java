package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.webbeans.DefinitionException;
import javax.webbeans.Fires;
import javax.webbeans.Initializer;
import javax.webbeans.Observer;
import javax.webbeans.Observes;
import javax.webbeans.Obtains;
import javax.webbeans.Produces;

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
         createBean(EnterpriseBean.of(annotatedClass, manager), annotatedClass, beans);
         beans.add(NewEnterpriseBean.of(annotatedClass, manager));
      }
      else if (isTypeSimpleWebBean(clazz))
      {
         createBean(SimpleBean.of(annotatedClass, manager), annotatedClass, beans);
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
    * @param beans The set of created beans
    */
   protected void createBean(AbstractClassBean<?> bean, AnnotatedClass<?> annotatedClass, Set<AbstractBean<?, ?>> beans)
   {
      
      beans.add(bean);
      
      manager.getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
      
      for (AnnotatedMethod<?> producerMethod : annotatedClass.getDeclaredAnnotatedMethods(Produces.class))
      {
         ProducerMethodBean<?> producerMethodBean = ProducerMethodBean.of(producerMethod, bean, manager);
         beans.add(producerMethodBean);
         manager.getResolver().addInjectionPoints(producerMethodBean.getAnnotatedInjectionPoints());
         registerEvents(producerMethodBean.getAnnotatedInjectionPoints(), beans);
         log.info("Web Bean: " + producerMethodBean);
      }
      for (AnnotatedField<?> producerField : annotatedClass.getDeclaredAnnotatedFields(Produces.class))
      {
         ProducerFieldBean<?> producerFieldBean = ProducerFieldBean.of(producerField, bean, manager);
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
            InstanceBean<Object, Field> instanceBean = InstanceBean.of((AnnotatedItem) injectionPoint, manager);
            beans.add(instanceBean);
            log.info("Web Bean: " + instanceBean);
         }
      }
      for (AnnotatedMethod<?> observerMethod : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Observes.class))
      {
         ObserverImpl<?> observer = ObserverImpl.of(observerMethod, bean, manager);
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
      manager.addObserver(observer, clazz, bindings);
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
         EventBean<Object, Method> eventBean = EventBean.of((AnnotatedItem) injectionPoint, manager);
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
