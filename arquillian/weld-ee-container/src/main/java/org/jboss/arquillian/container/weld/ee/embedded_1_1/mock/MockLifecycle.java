/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.helpers.ForwardingLifecycle;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.manager.BeanManagerImpl;

public class MockLifecycle extends ForwardingLifecycle
{

   private final WeldBootstrap bootstrap;
   private final BeanStore applicationBeanStore;
   private final BeanStore sessionBeanStore;
   private final BeanStore requestBeanStore;

   public MockLifecycle()
   {
      this.bootstrap = new WeldBootstrap();
      this.applicationBeanStore = new ConcurrentHashMapBeanStore();
      this.sessionBeanStore = new ConcurrentHashMapBeanStore();
      this.requestBeanStore = new ConcurrentHashMapBeanStore();
   }

   protected BeanStore getSessionBeanStore()
   {
      return sessionBeanStore;
   }

   protected BeanStore getRequestBeanStore()
   {
      return requestBeanStore;
   }

   protected BeanStore getApplicationBeanStore()
   {
      return applicationBeanStore;
   }

   public void initialize(Deployment deployment)
   {
      bootstrap.startContainer(getEnvironment(), deployment, getApplicationBeanStore());
   }

   @Override
   protected Lifecycle delegate()
   {
      return Container.instance().services().get(ContextLifecycle.class);
   }

   public Bootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   public BeanManagerImpl getBeanManager(BeanDeploymentArchive bda)
   {
      return bootstrap.getManager(bda);
   }

   public void beginApplication()
   {
      bootstrap.startInitialization().deployBeans().validateBeans().endInitialization();
   }

   @Override
   public void endApplication()
   {
      bootstrap.shutdown();
   }

   public void resetContexts()
   {

   }

   public void beginRequest()
   {
      delegate().beginRequest("Mock", getRequestBeanStore());
   }

   public void endRequest()
   {
      delegate().endRequest("Mock", getRequestBeanStore());
   }

   public void beginSession()
   {
      delegate().restoreSession("Mock", getSessionBeanStore());
   }

   public void endSession()
   {
      // TODO Conversation handling breaks this :-(
      //super.endSession("Mock", sessionBeanStore);
   }

   public Environment getEnvironment()
   {
      return Environments.EE_INJECT;
   }

}
