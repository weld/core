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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.event.ObserverFactory;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.introspector.WeldClass;
import org.jboss.weld.introspector.WeldMethod;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.DeploymentStructures;

/**
 * @author pmuir
 *
 */
public class ExtensionBeanDeployer
{
   
   private final BeanManagerImpl beanManager;
   private final Set<Extension> extensions;
   private final Deployment deployment;
   private final Map<BeanDeploymentArchive, BeanDeployment> beanDeployments;
   
   public ExtensionBeanDeployer(BeanManagerImpl manager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      this.beanManager = manager;
      this.extensions = new HashSet<Extension>();
      this.deployment = deployment;
      this.beanDeployments = beanDeployments;
   }
   
   public ExtensionBeanDeployer deployBeans()
   {
      ClassTransformer classTransformer = Container.instance().deploymentServices().get(ClassTransformer.class);
      for (Extension extension : extensions)
      {
         @SuppressWarnings("unchecked")
         WeldClass<Extension> clazz = (WeldClass<Extension>) classTransformer.loadClass(extension.getClass());
         
         // Locate the BeanDeployment for this extension
         BeanDeployment beanDeployment = DeploymentStructures.getOrCreateBeanDeployment(deployment, beanManager, beanDeployments, clazz.getJavaClass());
         
         ExtensionBean bean = new ExtensionBean(beanDeployment.getBeanManager(), clazz, extension);
         Set<ObserverMethodImpl<?, ?>> observerMethods = new HashSet<ObserverMethodImpl<?,?>>();
         createObserverMethods(bean, beanDeployment.getBeanManager(), clazz, observerMethods);
         beanDeployment.getBeanManager().addBean(bean);
         for (ObserverMethodImpl<?, ?> observerMethod : observerMethods)
         {
            observerMethod.initialize();
            beanDeployment.getBeanManager().addObserver(observerMethod);
         }
      }
      return this;
   }
   
   
   public void addExtensions(Iterable<Extension> extensions)
   {
      for (Extension extension : extensions)
      {
         addExtension(extension);
      }
   }
   
   public void addExtension(Extension extension)
   {
      this.extensions.add(extension);
   }
   
   protected <X> void createObserverMethods(RIBean<X> declaringBean, BeanManagerImpl beanManager, WeldClass<X> annotatedClass, Set<ObserverMethodImpl<?, ?>> observerMethods)
   {
      for (WeldMethod<?, X> method : annotatedClass.getDeclaredWeldMethodsWithAnnotatedParameters(Observes.class))
      {
         createObserverMethod(declaringBean, beanManager, method, observerMethods);
      }
   }
   
   protected <T, X> void createObserverMethod(RIBean<X> declaringBean, BeanManagerImpl beanManager, WeldMethod<T, X> method, Set<ObserverMethodImpl<?, ?>> observerMethods)
   {
      ObserverMethodImpl<T, X> observer = ObserverFactory.create(method, declaringBean, beanManager);
      observerMethods.add(observer);
   }

}
