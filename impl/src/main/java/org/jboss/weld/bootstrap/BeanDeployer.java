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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.messages.BootstrapMessage.BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR;

import java.util.HashSet;
import java.util.Set;

import javax.decorator.Decorator;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.interceptor.Interceptor;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.events.ProcessAnnotatedTypeImpl;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.InternalEjbDescriptor;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

/**
 * @author pmuir
 *
 */
public class BeanDeployer extends AbstractBeanDeployer<BeanDeployerEnvironment>
{
   
   private final Set<WeldClass<?>> classes;

   /**
    * @param manager
    * @param ejbDescriptors
    */
   public BeanDeployer(BeanManagerImpl manager, EjbDescriptors ejbDescriptors)
   {
      super(manager, new BeanDeployerEnvironment(ejbDescriptors, manager));
      this.classes = new HashSet<WeldClass<?>>();
   }

   public BeanDeployer addClass(Class<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      if (!clazz.isAnnotation() && !clazz.isEnum())
      {
         ProcessAnnotatedTypeImpl<?> event = ProcessAnnotatedTypeImpl.fire(getManager(), classTransformer.loadClass(clazz));
         if (!event.isVeto())
         {
            if (event.getAnnotatedType() instanceof WeldClass<?>)
            {
               classes.add((WeldClass<?>) event.getAnnotatedType());
            }
            else
            {
               classes.add(classTransformer.loadClass(event.getAnnotatedType()));
            }
         }
      }
      return this;
   }
   
   public BeanDeployer addClass(AnnotatedType<?> clazz)
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      classes.add(classTransformer.loadClass(clazz));
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

   public BeanDeployer createBeans()
   {
      for (WeldClass<?> clazz : classes)
      {
         boolean managedBeanOrDecorator = !getEnvironment().getEjbDescriptors().contains(clazz.getJavaClass()) && isTypeManagedBeanOrDecoratorOrInterceptor(clazz);
         if (managedBeanOrDecorator && clazz.isAnnotationPresent(Decorator.class))
         {
            validateDecorator(clazz);
            createDecorator(clazz);
         }
         else if (managedBeanOrDecorator && clazz.isAnnotationPresent(Interceptor.class))
         {
            validateInterceptor(clazz);
            createInterceptor(clazz);
         }
         else if (managedBeanOrDecorator && !clazz.isAbstract())
         {
            createManagedBean(clazz);
         }
      }
      for (InternalEjbDescriptor<?> ejbDescriptor : getEnvironment().getEjbDescriptors())
      {
         createSessionBean(ejbDescriptor);
      }
      
      // Now create the new beans
      for (WeldClass<?> clazz : getEnvironment().getNewManagedBeanClasses())
      {
         createNewManagedBean(clazz);
      }
      for (InternalEjbDescriptor<?> descriptor : getEnvironment().getNewSessionBeanDescriptors())
      {
         createNewSessionBean(descriptor);
      }
      
      return this;
   }

   private void validateInterceptor(WeldClass<?> clazz)
   {
      if (clazz.isAnnotationPresent(Decorator.class))
      {
         throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, clazz.getName());
      }
   }

   private void validateDecorator(WeldClass<?> clazz)
   {
      if (clazz.isAnnotationPresent(Interceptor.class))
      {
         throw new DeploymentException(BEAN_IS_BOTH_INTERCEPTOR_AND_DECORATOR, clazz.getName());
      }
   }

}
