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

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Control of the container, used for tests. Wraps up common operations.
 * 
 * If you require more control over the container bootstrap lifecycle you should
 * use the {@link #getLifecycle()} method. For example:
 * 
 * <code>TestContainer container = new TestContainer(...);
 * container.getLifecycle().initialize();
 * container.getLifecycle().getBootstrap().startInitialization();
 * container.getLifecycle().getBootstrap().deployBeans();
 * container.getLifecycle().getBootstrap().validateBeans();
 * container.getLifecycle().getBootstrap().endInitialization();
 * container.getLifecycle().stopContainer();</code>
 * 
 * Note that we can easily mix fine-grained calls to bootstrap, and coarse grained calls to {@link TestContainer}.
 * 
 * @author pmuir
 *
 */
public class TestContainer
{
   
   private final MockServletLifecycle lifecycle;
   
   /**
    * Create a container, specifying the classes and beans.xml to deploy
    * 
    * @param lifecycle
    * @param classes
    * @param beansXml
    */
   public TestContainer(MockServletLifecycle lifecycle, Collection<Class<?>> classes, Collection<URL> beansXml)
   {
      this(lifecycle);
      configureArchive(classes, beansXml);
   }
   
   public TestContainer(MockServletLifecycle lifecycle)
   {
      this.lifecycle = lifecycle;
   }
   
   public TestContainer(MockServletLifecycle lifecycle, Class<?>[] classes, URL[] beansXml)
   {
      this(lifecycle, classes == null ? null : Arrays.asList(classes), beansXml == null ? null : Arrays.asList(beansXml));
   }
   
   public TestContainer(MockServletLifecycle lifecycle, Class<?>... classes)
   {
      this(lifecycle, classes == null ? null : Arrays.asList(classes), null);
   }
   
   /**
    * Starts the container and begins the application
    */
   public TestContainer startContainer()
   {
      getLifecycle().initialize();
      getLifecycle().beginApplication();
      return this;
   }
   
   /**
    * Configure's the archive with the classes and beans.xml
    */
   protected TestContainer configureArchive(Collection<Class<?>> classes, Collection<URL> beansXml)
   {
      MockBeanDeploymentArchive archive = lifecycle.getWar();
      archive.setBeanClasses(classes);
      if (beansXml != null)
      {
         archive.setBeansXmlFiles(beansXml);
      }
      return this;
   }
   
   /**
    * Get the context lifecycle, allowing fine control over the contexts' state
    * 
    * @return
    */
   public MockServletLifecycle getLifecycle()
   {
      return lifecycle;
   }
   
   public BeanManagerImpl getBeanManager()
   {
      return getLifecycle().getBootstrap().getManager(getLifecycle().getWar());
   }
   
   public Deployment getDeployment()
   {
      return getLifecycle().getDeployment();
   }
   
   /**
    * Utility method which ensures a request is active and available for use
    * 
    */
   public TestContainer ensureRequestActive()
   {
      if (!getLifecycle().isSessionActive())
      {
         getLifecycle().beginSession();
      }
      if (!getLifecycle().isRequestActive())
      {
         getLifecycle().beginRequest();
      }
      return this;
   }

   /**
    * Clean up the container, ending any active contexts
    * 
    */
   public TestContainer stopContainer()
   {
      if (getLifecycle().isRequestActive())
      {
         getLifecycle().endRequest();
      }
      if (getLifecycle().isSessionActive())
      {
         getLifecycle().endSession();
      }
      if (getLifecycle().isApplicationActive())
      {
         getLifecycle().endApplication();
      }
      return this;
   }

}