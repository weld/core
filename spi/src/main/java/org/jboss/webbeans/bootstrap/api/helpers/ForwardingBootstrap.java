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
package org.jboss.webbeans.bootstrap.api.helpers;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.manager.api.WebBeansManager;

/**
 * Implementation of {@link Bootstrap} which supports the decorator pattern
 * @author Pete Muir
 *
 */
public abstract class ForwardingBootstrap implements Bootstrap
{
   
   protected abstract Bootstrap delegate();
   
   public WebBeansManager getManager(BeanDeploymentArchive beanDeploymentArchive)
   {
      return delegate().getManager(beanDeploymentArchive);
   }
   
   public Bootstrap startContainer(Environment environment, Deployment deployment, BeanStore beanStore)
   {
      return delegate().startContainer(environment, deployment, beanStore);
   }
   
   public void shutdown()
   {
      delegate().shutdown();
   }
   
   @Override
   public String toString()
   {
      return delegate().toString();
   }
   
   @Override
   public int hashCode()
   {
      return delegate().hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      return delegate().equals(obj);
   }

   public Bootstrap deployBeans()
   {
      return delegate().deployBeans();
   }

   public Bootstrap endInitialization()
   {
      return delegate().endInitialization();
   }

   public Bootstrap startInitialization()
   {
      return delegate().startInitialization();
   }

   public Bootstrap validateBeans()
   {
      return delegate().validateBeans();
   }
   
}
