/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.environment.se.discovery.WeldSEDeployment;
import org.jboss.weld.environment.se.discovery.url.URLScanner;
import org.jboss.weld.environment.se.discovery.url.WeldSEResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * An alternative means of booting WeldContainer form an arbitrary main method
 * within an SE application, <em>without</em> using the built-in
 * ContainerInitialized event. Typical usage of this API looks like this: <code>
 * WeldContainer weld = new Weld().initialize();
 * weld.instance().select(Foo.class).get();
 * weld.event().select(Bar.class).fire(new Bar());
 * weld.shutdown();
 * </code>
 * 
 * @author Peter Royle
 */
public class Weld
{

   protected static final String[] RESOURCES = { "META-INF/beans.xml" };

   private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";

   private ShutdownManager shutdownManager;

   /**
    * Boots Weld and creates and returns a WeldContainer instance, through which
    * beans and events can be accessed.
    */
   @PostConstruct
   public WeldContainer initialize()
   {

      BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
      WeldSEDeployment deployment = createDeployment();

      Bootstrap bootstrap = null;
      try
      {
         bootstrap = (Bootstrap) deployment.getServices().get(ResourceLoader.class).classForName(BOOTSTRAP_IMPL_CLASS_NAME).newInstance();
      }
      catch (InstantiationException ex)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
      }
      catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
      }

      
      // Kick off the scan
      deployment.getScanner().scan(deployment.getServices().get(ResourceLoader.class));
      
      // Set up the container
      bootstrap.startContainer(Environments.SE, deployment, applicationBeanStore);
      
      // Start the container
      bootstrap.startInitialization();
      bootstrap.deployBeans();
      getInstanceByType(bootstrap.getManager(deployment.loadBeanDeploymentArchive(ShutdownManager.class)), ShutdownManager.class).setBootstrap(bootstrap);
      bootstrap.validateBeans();
      bootstrap.endInitialization();

      // Set up the ShutdownManager for later
      this.shutdownManager = getInstanceByType(bootstrap.getManager(deployment.loadBeanDeploymentArchive(ShutdownManager.class)), ShutdownManager.class);

      return getInstanceByType(bootstrap.getManager(deployment.loadBeanDeploymentArchive(WeldContainer.class)), WeldContainer.class);
   }

   /**
    * Users can subclass and override this method to customise the deployment
    * before weld boots up. For example, to add a custom ResourceLoader, you
    * would subclass Weld like so: <code>
    * public class MyWeld extends Weld {
    * 
    * @Override protected WeldSEDeployment createDeployment() { WeldSEDeployment
    *           myDeployment = super.createDeployment();
    *           deployment.getServices().add(ResourceLoader.class, new
    *           OSGIResourceLoader()); } } </code>
    */
   protected WeldSEDeployment createDeployment()
   {
      WeldSEDeployment deployment = new WeldSEDeployment(new URLScanner(RESOURCES));
      deployment.getServices().add(ResourceLoader.class, new WeldSEResourceLoader());
      return deployment;
   }
   
   protected <T> T getInstanceByType(BeanManager manager, Class<T> type, Annotation... bindings)
   {
      final Bean<?> bean = manager.resolve(manager.getBeans(type));
      if (bean == null)
      {
         throw new UnsatisfiedResolutionException("Unable to resolve a bean for " + type + " with bindings " + Arrays.asList(bindings));
      }
      CreationalContext<?> cc = manager.createCreationalContext(bean);
      return type.cast(manager.getReference(bean, type, cc));
   }

   /**
    * Shuts down Weld.
    */
   public void shutdown()
   {
      shutdownManager.shutdown();
   }
}
