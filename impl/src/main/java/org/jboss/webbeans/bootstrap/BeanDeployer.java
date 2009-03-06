package org.jboss.webbeans.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.event.Fires;
import javax.event.Observes;
import javax.inject.BindingType;
import javax.inject.DeploymentType;
import javax.inject.Initializer;
import javax.inject.Obtains;
import javax.inject.Produces;
import javax.inject.Realizes;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.EnterpriseBean;
import org.jboss.webbeans.bean.EventBean;
import org.jboss.webbeans.bean.InstanceBean;
import org.jboss.webbeans.bean.NewEnterpriseBean;
import org.jboss.webbeans.bean.NewSimpleBean;
import org.jboss.webbeans.bean.ProducerFieldBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bean.RIBean;
import org.jboss.webbeans.bean.SimpleBean;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.event.ObserverFactory;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedItem;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.WrappedAnnotatedField;
import org.jboss.webbeans.introspector.WrappedAnnotatedMethod;
import org.jboss.webbeans.introspector.jlr.AnnotatedClassImpl;
import org.jboss.webbeans.jsf.JSFApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.servlet.ServletApiAbstraction;

public class BeanDeployer
{
   
   private static final LogProvider log = Logging.getLogProvider(BeanDeployer.class);
   
   private final Set<RIBean<?>> beans;
   private final Set<AnnotatedClass<?>> deferredClasses;
   private final ManagerImpl manager;
   private EventBean<Object, Method> eventBean;
   
   public BeanDeployer(ManagerImpl manager)
   {
      this.manager = manager;
      this.beans = new HashSet<RIBean<?>>();
      this.deferredClasses = new HashSet<AnnotatedClass<?>>();
   }
  
   
   public void addBean(RIBean<?> bean)
   {
      this.beans.add(bean);
   }
   
   public void addClass(Class<?> clazz)
   {
      if (!clazz.isAnnotation())
      {
         deferredClasses.add(AnnotatedClassImpl.of(clazz));
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
      for (AnnotatedClass<?> clazz : deferredClasses)
      {
         if (manager.getEjbDescriptorCache().containsKey(clazz.getType()))
         {
            createEnterpriseBean(clazz);
         }
         else if (isTypeSimpleWebBean(clazz))
         {
            createSimpleBean(clazz);
         }
      }
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
      
      manager.getResolver().addInjectionPoints(bean.getInjectionPoints());
      
      createProducerMethods(bean, annotatedClass);
      createProducerFields(bean, annotatedClass);
      createObserverMethods(bean, annotatedClass);
      createFacades(bean.getInjectionPoints());
      
      if (annotatedClass.isAnnotationPresent(Realizes.class))
      {
         createRealizedProducerMethods(bean, annotatedClass);
         createRealizedProducerFields(bean, annotatedClass);
         createRealizedObserverMethods(bean, annotatedClass);
      }
      
      log.info("Web Bean: " + bean);
   }
   
   private void createProducerMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedMethod<?> method : annotatedClass.getDeclaredAnnotatedMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);
         
      }
   }
   
   private void createProducerMethod(AbstractClassBean<?> declaringBean, AnnotatedMethod<?> annotatedMethod)
   {
      ProducerMethodBean<?> bean = ProducerMethodBean.of(annotatedMethod, declaringBean, manager);
      beans.add(bean);
      manager.getResolver().addInjectionPoints(bean.getInjectionPoints());
      createFacades(bean.getInjectionPoints());
      log.info("Web Bean: " + bean);
   }
   
   private void createRealizedProducerMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> realizingClass)
   {
      AnnotatedClass<?> realizedClass = realizingClass.getSuperclass();
      for (AnnotatedMethod<?> realizedMethod : realizedClass.getDeclaredAnnotatedMethods(Produces.class))
      { 
         createProducerMethod(declaringBean, realizeProducerMethod(realizedMethod, realizingClass));
      }
   }
   
   private void createRealizedProducerFields(AbstractClassBean<?> declaringBean, AnnotatedClass<?> realizingClass)
   {
      AnnotatedClass<?> realizedClass = realizingClass.getSuperclass();
      for (final AnnotatedField<?> realizedField : realizedClass.getDeclaredAnnotatedFields(Produces.class))
      { 
         createProducerField(declaringBean, realizeProducerField(realizedField, realizingClass));
      }
   }
   
   private void createProducerField(AbstractClassBean<?> declaringBean, AnnotatedField<?> field)
   {
      ProducerFieldBean<?> bean = ProducerFieldBean.of(field, declaringBean, manager);
      beans.add(bean);
      log.info("Web Bean: " + bean);
   }
   
   private void createProducerFields(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedField<?> field : annotatedClass.getDeclaredAnnotatedFields(Produces.class))
      {
         createProducerField(declaringBean, field);
      }
   }

   private void createObserverMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedMethod<?> method : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, method);
      }
   }
   
   private void createRealizedObserverMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> realizingClass)
   {
      createObserverMethods(declaringBean, realizingClass.getSuperclass());
   }
   
   private void createObserverMethod(AbstractClassBean<?> declaringBean, AnnotatedMethod<?> method)
   {
      ObserverImpl<?> observer = ObserverFactory.create(method, declaringBean, manager);
      manager.addObserver(observer);
   }
   
   private void createSimpleBean(AnnotatedClass<?> annotatedClass)
   {
      SimpleBean<?> bean = SimpleBean.of(annotatedClass, manager);
      createBean(bean, annotatedClass);
      beans.add(NewSimpleBean.of(annotatedClass, manager));
   }
   
   private void createEnterpriseBean(AnnotatedClass<?> annotatedClass)
   {
      EnterpriseBean<?> bean = EnterpriseBean.of(annotatedClass, manager);
      createBean(bean, annotatedClass);
      beans.add(NewEnterpriseBean.of(annotatedClass, manager));
   }

   private void createFacades(Set<? extends AnnotatedItem<?, ?>> injectionPoints)
   {
      for (AnnotatedItem<?, ?> injectionPoint : injectionPoints)
      {
         if (injectionPoint.isAnnotationPresent(Fires.class))
         {
             createEvent(injectionPoint);
         }
         if (injectionPoint.isAnnotationPresent(Obtains.class))
         {
            createInstance(injectionPoint);
         }
      }
   }

   private void createEvent(AnnotatedItem<?, ?> injectionPoint)
   {
      if (eventBean == null)
      {
         // TODO Fix this!
         @SuppressWarnings("unchecked")
         EventBean<Object, Method> bean = EventBean.of((AnnotatedItem) injectionPoint, manager);
         beans.add(bean);
         log.info("Web Bean: " + bean);
         eventBean = bean;
      }
      else
      {
         eventBean.addBindings(injectionPoint.getBindings());
      }
   }
   
   private void createInstance(AnnotatedItem<?, ?> injectionPoint)
   {
      // TODO FIx this
      @SuppressWarnings("unchecked")
      InstanceBean<Object, ?> bean = InstanceBean.of((AnnotatedItem) injectionPoint, manager);
      beans.add(bean);
      log.info("Web Bean: " + bean);
   }
   
   /**
    * Indicates if the type is a simple Web Bean
    * 
    * @param type The type to inspect
    * @return True if simple Web Bean, false otherwise
    */
   private boolean isTypeSimpleWebBean(AnnotatedClass<?> clazz)
   {
      EJBApiAbstraction ejbApiAbstraction = new EJBApiAbstraction(manager.getResourceLoader());
      JSFApiAbstraction jsfApiAbstraction = new JSFApiAbstraction(manager.getResourceLoader());
      ServletApiAbstraction servletApiAbstraction = new ServletApiAbstraction(manager.getResourceLoader());
      // TODO: check 3.2.1 for more rules!!!!!!
      return !clazz.isAbstract() && !clazz.isParameterizedType() && !servletApiAbstraction.SERVLET_CLASS.isAssignableFrom(clazz) && !servletApiAbstraction.FILTER_CLASS.isAssignableFrom(clazz) && !servletApiAbstraction.SERVLET_CONTEXT_LISTENER_CLASS.isAssignableFrom(clazz) && !servletApiAbstraction.HTTP_SESSION_LISTENER_CLASS.isAssignableFrom(clazz) && !servletApiAbstraction.SERVLET_REQUEST_LISTENER_CLASS.isAssignableFrom(clazz) && !ejbApiAbstraction.ENTERPRISE_BEAN_CLASS.isAssignableFrom(clazz) && !jsfApiAbstraction.UICOMPONENT_CLASS.isAssignableFrom(clazz) && hasSimpleWebBeanConstructor(clazz);
   }
   


   private static boolean hasSimpleWebBeanConstructor(AnnotatedClass<?> type)
   {
      return type.getConstructor() != null || type.getAnnotatedConstructors(Initializer.class).size() > 0; 
   }
   
   private static <T> AnnotatedMethod<T> realizeProducerMethod(final AnnotatedMethod<T> method, final AnnotatedClass<?> realizingClass)
   {
      return new WrappedAnnotatedMethod<T>(method, realizingClass.getMetaAnnotations(BindingType.class))
      {
         
         @Override
         public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
         {
            if (metaAnnotationType.equals(DeploymentType.class))
            {
               return realizingClass.getMetaAnnotations(DeploymentType.class);
            }
            else
            {
               return super.getMetaAnnotations(metaAnnotationType);
            }
         }
         
         @Override
         public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
         {
            if (metaAnnotationType.equals(DeploymentType.class))
            {
               return realizingClass.getDeclaredMetaAnnotations(DeploymentType.class);
            }
            else
            {
               return super.getDeclaredMetaAnnotations(metaAnnotationType);
            }
         }
         
      };
   }
   
   private static <T> AnnotatedField<T> realizeProducerField(final AnnotatedField<T> field, final AnnotatedClass<?> realizingClass)
   {
      return new WrappedAnnotatedField<T>(field, realizingClass.getMetaAnnotations(BindingType.class))
      {
         
         @Override
         public Set<Annotation> getMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
         {
            if (metaAnnotationType.equals(DeploymentType.class))
            {
               return realizingClass.getMetaAnnotations(DeploymentType.class);
            }
            else
            {
               return super.getMetaAnnotations(metaAnnotationType);
            }
         }
         
         @Override
         public Set<Annotation> getDeclaredMetaAnnotations(Class<? extends Annotation> metaAnnotationType)
         {
            if (metaAnnotationType.equals(DeploymentType.class))
            {
               return realizingClass.getDeclaredMetaAnnotations(DeploymentType.class);
            }
            else
            {
               return super.getDeclaredMetaAnnotations(metaAnnotationType);
            }
         }
         
      };
   }
   
}
