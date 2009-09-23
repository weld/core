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

import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.Container;
import org.jboss.webbeans.ejb.EjbDescriptors;
import org.jboss.webbeans.ejb.InternalEjbDescriptor;
import org.jboss.webbeans.introspector.WBClass;
import org.jboss.webbeans.resources.ClassTransformer;

/**
 * @author pmuir
 *
 */
public class BeanDeployer extends AbstractBeanDeployer
{
   
   private final BeanManagerImpl deploymentManager;

   private final Set<WBClass<?>> classes;

   /**
    * @param manager
    * @param ejbDescriptors
    */
   public BeanDeployer(BeanManagerImpl manager, BeanManagerImpl deploymentManager, EjbDescriptors ejbDescriptors)
   {
      super(manager, new BeanDeployerEnvironment(ejbDescriptors, manager));
      this.classes = new HashSet<WBClass<?>>();
      this.deploymentManager = deploymentManager;
   }

   public AbstractBeanDeployer addClass(Class<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      if (!clazz.isAnnotation() && !clazz.isEnum())
      {
         ProcessAnnotatedTypeImpl<?> event = createProcessAnnotatedTypeEvent(clazz, classTransformer);
         deploymentManager.fireEvent(event);
         if (!event.isVeto())
         {
            if (event.getAnnotatedType() instanceof WBClass<?>)
            {
               classes.add((WBClass<?>) event.getAnnotatedType());
            }
            else
            {
               classes.add(classTransformer.loadClass(event.getAnnotatedType()));
            }
         }
      }
      return this;
   }
   
   private static <X> ProcessAnnotatedTypeImpl<X> createProcessAnnotatedTypeEvent(Class<X> clazz, ClassTransformer classTransformer)
   {
      WBClass<X> annotatedType = classTransformer.loadClass(clazz);
      return new ProcessAnnotatedTypeImpl<X>(annotatedType) {};
   }
   
   // TODO Do we need to fire PAT for annotated types added via BBD? Probably not PLM.
   public AbstractBeanDeployer addClass(AnnotatedType<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      classes.add(classTransformer.loadClass(clazz));
      return this;
   }

   public AbstractBeanDeployer addClasses(Iterable<Class<?>> classes)
   {
      for (Class<?> clazz : classes)
      {
         addClass(clazz);
      }
      return this;
   }

   public AbstractBeanDeployer createBeans()
   {
      for (WBClass<?> clazz : classes)
      {
         boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(clazz.getJavaClass()) && isTypeManagedBeanOrDecorator(clazz);
         if (managedBeanOrDecorator && clazz.isAnnotationPresent(Decorator.class))
         {
            createDecorator(clazz);
         }
         else if (managedBeanOrDecorator && clazz.isAnnotationPresent(Interceptor.class))
         {
            //createInterceptor(clazz);
         }
         else if (managedBeanOrDecorator && !clazz.isAbstract())
         {
            createSimpleBean(clazz);
         }
      }
      for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors())
      {
         createEnterpriseBean(ejbDescriptor);
      }
      return this;
   }

}
