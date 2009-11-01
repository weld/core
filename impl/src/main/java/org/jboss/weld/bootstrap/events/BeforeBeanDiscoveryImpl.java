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

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.literal.BindingTypeLiteral;
import org.jboss.weld.literal.InterceptorBindingTypeLiteral;
import org.jboss.weld.literal.NormalScopeLiteral;
import org.jboss.weld.literal.ScopeLiteral;

public class BeforeBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements BeforeBeanDiscovery
{

   public static void fire(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      new BeforeBeanDiscoveryImpl(beanManager, deployment, beanDeployments).fire();
   }

   protected BeforeBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments)
   {
      super(beanManager, BeforeBeanDiscovery.class, beanDeployments, deployment);
   }

   public void addQualifier(Class<? extends Annotation> bindingType)
   {
      getTypeStore().add(bindingType, new BindingTypeLiteral());
   }

   public void addInterceptorBinding(Class<? extends Annotation> bindingType)
   {
      getTypeStore().add(bindingType, new InterceptorBindingTypeLiteral());
   }

   public void addScope(Class<? extends Annotation> scopeType, boolean normal, boolean passivating)
   {
      if (normal)
      {
         getTypeStore().add(scopeType, new NormalScopeLiteral(passivating));
      }
      else
      {
         getTypeStore().add(scopeType, new ScopeLiteral());
      }
   }

   public void addStereotype(Class<? extends Annotation> stereotype, Annotation... stereotypeDef)
   {
      throw new UnsupportedOperationException();
   }

   public void addAnnotatedType(AnnotatedType<?> type)
   {
      getOrCreateBeanDeployment(type.getJavaClass()).getBeanDeployer().addClass(type);
   }

}
