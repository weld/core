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
package org.jboss.weld.mock;

import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.helpers.ForwardingLifecycle;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.servlet.api.ServletServices;

public class MockServletLifecycle extends ForwardingLifecycle implements MockLifecycle
{
   private static final ResourceLoader MOCK_RESOURCE_LOADER = new MockResourceLoader();
   
   private final WeldBootstrap bootstrap;
   private final Deployment deployment;
   private final MockBeanDeploymentArchive war;
   private final BeanStore applicationBeanStore;
   private final BeanStore sessionBeanStore;
   private final BeanStore requestBeanStore;
   
   private Lifecycle lifecycle;
   
   public MockServletLifecycle()
   {
      this(new MockBeanDeploymentArchive());
   }
   
   private MockServletLifecycle(MockBeanDeploymentArchive war)
   {
      this(new MockDeployment(war), war);
   }
   
   public MockServletLifecycle(Deployment deployment, MockBeanDeploymentArchive war)
   {
      this.deployment = deployment;
      this.war = war;
      if (deployment == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery is available");
      }
      this.bootstrap = new WeldBootstrap();
      this.deployment.getServices().add(ResourceLoader.class, MOCK_RESOURCE_LOADER);
      this.deployment.getServices().add(ServletServices.class, new MockServletServices(war));
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
   
   public void initialize()
   {
      try
      {
         bootstrap.startContainer(getEnvironment(), getDeployment(), getApplicationBeanStore());
      }
      finally  
      {
         lifecycle = deployment.getServices().get(ContextLifecycle.class);
      }
   }
   
   @Override
   protected Lifecycle delegate()
   {
      return lifecycle;
   }
   
   protected Deployment getDeployment()
   {
      return deployment;
   }
   
   public WeldBootstrap getBootstrap()
   {
      return bootstrap;
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
      super.beginRequest("Mock", getRequestBeanStore());
   }
   
   public void endRequest()
   {
      super.endRequest("Mock", getRequestBeanStore());
   }
   
   public void beginSession()
   {
      super.restoreSession("Mock", getSessionBeanStore());
   }
   
   public void endSession()
   {
      // TODO Conversation handling breaks this :-(
      //super.endSession("Mock", sessionBeanStore);
   }
   
   protected Environment getEnvironment()
   {
      return Environments.SERVLET;
   }
   
   public MockBeanDeploymentArchive getWar()
   {
      return war;
   }
}
