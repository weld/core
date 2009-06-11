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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.BindingType;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Initializer;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.deployment.DeploymentType;
import javax.event.Observes;
import javax.inject.Realizes;

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
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.event.ObserverFactory;
import org.jboss.webbeans.event.ObserverImpl;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.introspector.AnnotatedField;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.introspector.WrappedAnnotatedField;
import org.jboss.webbeans.introspector.WrappedAnnotatedMethod;
import org.jboss.webbeans.jsf.JsfApiAbstraction;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.persistence.spi.EntityDiscovery;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.util.Reflections;

public class BeanDeployer
{
   
   private static final LogProvider log = Logging.getLogProvider(BeanDeployer.class);
   
   private final BeanDeployerEnvironment environment;
   private final Set<AnnotatedClass<?>> classes;
   private final BeanManagerImpl manager;
   private final ClassTransformer classTransformer;
   
   
   public BeanDeployer(BeanManagerImpl manager, EjbDescriptorCache ejbDescriptors)
   {
      this.manager = manager;
      this.environment = new BeanDeployerEnvironment(ejbDescriptors);
      this.classes = new HashSet<AnnotatedClass<?>>();
      this.classTransformer = new ClassTransformer();
   }
   
   public <T> BeanDeployer addBean(RIBean<T> bean)
   {
      this.environment.addBean(bean);
      return this;
   }
   
   public BeanDeployer addBeans(Iterable<? extends RIBean<?>> beans)
   {
      for (RIBean<?> bean : beans)
      {
         addBean(bean);
      }
      return this;
   }
   
   public BeanDeployer addClass(Class<?> clazz)
   {
      if (!clazz.isAnnotation() && !clazz.isEnum())
      {
         classes.add(classTransformer.classForName(clazz));
      }
      return this;
   }
   
   public BeanDeployer addClasses(Iterable<Class<?>> classes)
   {
      for (Class<?> clazz : classes)
      {
         addClass(clazz);
      }
      return this;
   }
   
   public BeanDeployer addClasses(Collection<AnnotatedClass<?>> classes)
   {
      classes.addAll(classes);
      return this;
   }
   
   public BeanDeployer createBeans()
   {
      for (AnnotatedClass<?> clazz : classes)
      {
         if (environment.getEjbDescriptors().containsKey(clazz.getRawType()))
         {
            createEnterpriseBean(clazz);
         }
         else
         {
            boolean managedBeanOrDecorator = isTypeManagedBeanOrDecorator(clazz);
            if (managedBeanOrDecorator && clazz.isAnnotationPresent(Decorator.class))
            {
               createDecorator(clazz);
            }
            else if (managedBeanOrDecorator && !clazz.isAbstract())
            {
               createSimpleBean(clazz);
            }
         }
      }
      return this;
   }

   public BeanDeployer deploy()
   {
      Set<RIBean<?>> beans = environment.getBeans();
      for (RIBean<?> bean : beans)
      {
         bean.initialize(environment);
         log.debug("Bean: " + bean);
      }
      manager.setBeans(beans);
      for (ObserverImpl<?> observer : environment.getObservers())
      {
         observer.initialize();
         log.debug("Observer : " + observer);
         manager.addObserver(observer);
      }
      
      // TODO: move to boot
      checkDisposalMethods();
      
      return this;
   }

   
   private void checkDisposalMethods() {
	      Set<DisposalMethodBean<?>> all = new HashSet<DisposalMethodBean<?>>(environment.getAllDisposalBeans()); 
	      Set<DisposalMethodBean<?>> resolved = new HashSet<DisposalMethodBean<?>>(environment.getResolvedDisposalBeans());
	      if(all.size()>0 && !resolved.containsAll(all)) {
	         StringBuffer buff = new StringBuffer();
	         buff.append("The following Disposal methods where not resolved\n");
	         all.removeAll(resolved);
	         for(DisposalMethodBean<?> bean: all) {
	            buff.append(bean.toString());
	         }
	         throw new UnsatisfiedResolutionException(buff.toString());
	      }
	   }   
   
   public BeanDeployerEnvironment getBeanDeployerEnvironment()
   {
      return environment;
   }
   
   /**
    * Creates a Web Bean from a bean abstraction and adds it to the set of
    * created beans
    * 
    * Also creates the implicit field- and method-level beans, if present
    * 
    * @param bean
    *           The bean representation
    */
   protected <T> void createBean(AbstractClassBean<T> bean, final AnnotatedClass<T> annotatedClass)
   {
      
      addBean(bean);
      
      manager.getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
      
      createProducerMethods(bean, annotatedClass);
      createProducerFields(bean, annotatedClass);
      createObserverMethods(bean, annotatedClass);
      createDisposalMethods(bean, annotatedClass);
      
      if (annotatedClass.isAnnotationPresent(Realizes.class))
      {
         createRealizedProducerMethods(bean, annotatedClass);
         createRealizedProducerFields(bean, annotatedClass);
         createRealizedObserverMethods(bean, annotatedClass);
      }
   }
   
   private void createProducerMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedMethod<?> method : annotatedClass.getDeclaredAnnotatedMethods(Produces.class))
      {
         createProducerMethod(declaringBean, method);         
      }
   }
   
   private void createDisposalMethods(AbstractClassBean<?> declaringBean, AnnotatedClass<?> annotatedClass)
   {
      for (AnnotatedMethod<?> method : annotatedClass.getDeclaredMethodsWithAnnotatedParameters(Disposes.class))
      {
         DisposalMethodBean<?> disposalBean = DisposalMethodBean.of(manager, method, declaringBean);
         environment.addAllDisposalBean(disposalBean);
         manager.getResolver().addInjectionPoints(disposalBean.getAnnotatedInjectionPoints());
         manager.addBean(disposalBean);
      }
   }
   
   private <T> void createProducerMethod(AbstractClassBean<?> declaringBean, AnnotatedMethod<T> annotatedMethod)
   {
      ProducerMethodBean<T> bean = ProducerMethodBean.of(annotatedMethod, declaringBean, manager);
      addBean(bean);
      manager.getResolver().addInjectionPoints(bean.getAnnotatedInjectionPoints());
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
   
   private <T> void createProducerField(AbstractClassBean<?> declaringBean, AnnotatedField<T> field)
   {
      ProducerFieldBean<T> bean = ProducerFieldBean.of(field, declaringBean, manager);
      addBean(bean);
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
      environment.getObservers().add(observer);
   }
   
   private <T> void createSimpleBean(AnnotatedClass<T> annotatedClass)
   {
      SimpleBean<T> bean = SimpleBean.of(annotatedClass, manager);
      createBean(bean, annotatedClass);
      addBean(NewSimpleBean.of(annotatedClass, manager));
   }
   
   private <T> void createDecorator(AnnotatedClass<T> annotatedClass)
   {
      DecoratorBean<T> bean = DecoratorBean.of(annotatedClass, manager);
      addBean(bean);
   }
   
   private <T> void createEnterpriseBean(AnnotatedClass<T> annotatedClass)
   {
      // TODO Don't create enterprise bean if it has no local interfaces!
      EnterpriseBean<T> bean = EnterpriseBean.of(annotatedClass, manager, environment);
      createBean(bean, annotatedClass);
      addBean(NewEnterpriseBean.of(annotatedClass, manager, environment));
   }
   
   /**
    * Indicates if the type is a simple Web Bean
    * 
    * @param type
    *           The type to inspect
    * @return True if simple Web Bean, false otherwise
    */
   private boolean isTypeManagedBeanOrDecorator(AnnotatedClass<?> clazz)
   {
      Class<?> rawType = clazz.getRawType();
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
             hasSimpleWebBeanConstructor(clazz) &&
             !isEntity(clazz);
   }
   
   private boolean isEntity(AnnotatedClass<?> clazz)
   {
	   if (manager.getServices().contains(EntityDiscovery.class))
	   {
		   EntityDiscovery entityDiscovery = manager.getServices().get(EntityDiscovery.class);
		   return 
		   		entityDiscovery.discoverEntitiesFromAnnotations().contains(clazz.getRawType()) || 
		   		entityDiscovery.discoverEntitiesFromPersistenceUnits().contains(clazz.getRawType()) || 
		   		entityDiscovery.discoverEntitiesFromXml().contains(clazz.getRawType());
	   }
	   else
	   {
		   return false;
	   }
   }
   
   private static boolean hasSimpleWebBeanConstructor(AnnotatedClass<?> type)
   {
      return type.getNoArgsConstructor() != null || type.getAnnotatedConstructors(Initializer.class).size() > 0;
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
