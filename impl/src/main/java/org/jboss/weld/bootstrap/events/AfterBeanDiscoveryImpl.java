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
package org.jboss.weld.bootstrap.events;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.Decorator;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.AnnotatedItemProvidingDecoratorWrapper;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

public class AfterBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements AfterBeanDiscovery
{
   
   public static void fire(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      new AfterBeanDiscoveryImpl(beanManager, deployment, beanDeployments).fire(beanDeployments);
   }
   
   protected AfterBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      super(beanManager, AfterBeanDiscovery.class, beanDeployments, deployment);
   }

   public void addDefinitionError(Throwable t)
   {
      getErrors().add(t);
   }
   
   public List<Throwable> getDefinitionErrors()
   {
      return Collections.unmodifiableList(getErrors());
   }
   
   public void addBean(Bean<?> bean)
   {
      BeanManagerImpl beanManager = getOrCreateBeanDeployment(bean.getBeanClass()).getBeanManager();
      if (bean instanceof Interceptor<?>)
      {
         beanManager.addInterceptor((Interceptor<?>) bean);
      }
      else if (bean instanceof Decorator)
      {
         beanManager.addDecorator(AnnotatedItemProvidingDecoratorWrapper.of((Decorator<?>)bean, beanManager));
      }
      else
      {
         beanManager.addBean(bean);
      }
      ProcessBeanImpl.fire(beanManager, bean);
   }

   public void addContext(Context context)
   {
      getBeanManager().addContext(context);
   }

   public void addObserverMethod(ObserverMethod<?> observerMethod)
   {
      getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager().addObserver(observerMethod);
   }

}
