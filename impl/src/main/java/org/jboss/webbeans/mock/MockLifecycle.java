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
package org.jboss.webbeans.mock;



import org.jboss.webbeans.bootstrap.WebBeansBootstrap;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.AbstractLifecycle;
import org.jboss.webbeans.transaction.spi.TransactionServices;

public class MockLifecycle extends AbstractLifecycle
{ 
   
   private static final EjbServices MOCK_EJB_RESOLVER = new MockEjBServices();
   private static final ResourceLoader MOCK_RESOURCE_LOADER = new MockResourceLoader();
   private static final TransactionServices MOCK_TRANSACTION_SERVICES = new MockTransactionServices();
   
   private final WebBeansBootstrap bootstrap;
   private final MockWebBeanDiscovery webBeanDiscovery;
   private BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
   private BeanStore sessionBeanStore = new ConcurrentHashMapBeanStore();
   private BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
   
   public MockLifecycle()
   {
      this(new MockWebBeanDiscovery());
   }
   
   public MockLifecycle(MockWebBeanDiscovery mockWebBeanDiscovery)
   {
      this.webBeanDiscovery = mockWebBeanDiscovery;
      if (webBeanDiscovery == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery is available");
      }
      bootstrap = new WebBeansBootstrap();
      bootstrap.setNamingContext(new MockNamingContext(null));
      bootstrap.setEjbServices(MOCK_EJB_RESOLVER);
      bootstrap.setResourceLoader(MOCK_RESOURCE_LOADER);
      bootstrap.setWebBeanDiscovery(webBeanDiscovery);
      bootstrap.setApplicationContext(applicationBeanStore);
      bootstrap.setTransactionServices(MOCK_TRANSACTION_SERVICES);
      bootstrap.initialize();
   }
   
   public MockWebBeanDiscovery getWebBeanDiscovery()
   {
      return webBeanDiscovery;
   }
   
   public WebBeansBootstrap getBootstrap()
   {
      return bootstrap;
   }
   
   public void beginApplication()
   {
      bootstrap.setEjbDiscovery(new MockEjbDiscovery(webBeanDiscovery.discoverWebBeanClasses()));
      bootstrap.boot();
   }
   
   public void endApplication()
   {
      
   }
   
   public void resetContexts()
   {
      
   }
   
   public void beginRequest()
   {
      super.beginRequest("Mock", requestBeanStore);
   }
   
   public void endRequest()
   {
      super.endRequest("Mock", requestBeanStore);
   }
   
   public void beginSession()
   {
      super.restoreSession("Mock", sessionBeanStore);
   }
   
   public void endSession()
   {
      // TODO Conversation handling breaks this :-(
      //super.endSession("Mock", sessionBeanStore);
   }
   
}
