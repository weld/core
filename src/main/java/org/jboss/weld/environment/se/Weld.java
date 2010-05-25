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

import javax.annotation.PostConstruct;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.environment.se.beans.InstanceManager;
import org.jboss.weld.environment.se.discovery.NewSEWeldDeployment;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.URLScanner;
import org.jboss.weld.environment.se.util.WeldManagerUtils;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * An alternative means of booting WeldContainer form an arbitrary main method within an
 * SE application, <em>without</em> using the built-in ContainerInitialized event.
 * Typical usage of this API looks like this:
 * <code>
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

   private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";
   private WeldManager manager;

   public Weld()
   {
   }

   /**
    * Boots Weld and creates and returns a WeldContainer instance, through which
    * beans and events can be accessed.
    */
   @PostConstruct
   public WeldContainer initialize()
   {

      BeanStore applicationBeanStore = new ConcurrentHashMapBeanStore();
      Deployment deployment = createDeployment();

      Bootstrap bootstrap = null;
      try
      {
         bootstrap = (Bootstrap) deployment.getServices().get(ResourceLoader.class).classForName(BOOTSTRAP_IMPL_CLASS_NAME).newInstance();
      } catch (InstantiationException ex)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
      } catch (IllegalAccessException ex)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", ex);
      }

      BeanDeploymentArchive discovery = discoverBeansAndResources(deployment);
      // transfer discovered classes and resources to the deployment in a single BeanDeploymentArchive
      deployment.getBeanDeploymentArchives().add(discovery);

      bootstrap.startContainer(Environments.SE, deployment, applicationBeanStore);
      final BeanDeploymentArchive mainBeanDepArch = deployment.getBeanDeploymentArchives().iterator().next();
      this.manager = bootstrap.getManager(mainBeanDepArch);
      bootstrap.startInitialization();
      bootstrap.deployBeans();
      WeldManagerUtils.getInstanceByType(manager, ShutdownManager.class).setBootstrap(bootstrap);
      bootstrap.validateBeans();
      bootstrap.endInitialization();

      InstanceManager instanceManager = WeldManagerUtils.getInstanceByType(manager, InstanceManager.class);

      return new WeldContainer(instanceManager, manager);

   }

   /*
    * Users can subclass and override this method to customise the classes and
    * resources that Weld finds when it boots up. 
    */
   protected BeanDeploymentArchive discoverBeansAndResources(Deployment deployment)
   {
      WeldSEBeanDeploymentArchive discovery = new WeldSEBeanDeploymentArchive("weld-se-main-archive");
      final ResourceLoader resourceLoader = deployment.getServices().get(ResourceLoader.class);
      URLScanner scanner = new URLScanner(resourceLoader, discovery);
      scanner.scanResources(new String[]
              {
                 "META-INF/beans.xml"
              });
      return discovery;
   }

   /**
    * Users can subclass and override this method to customise the deployment
    * before weld boots up. For example, to add a custom ResourceLoader, you would
    * subclass Weld like so:
    * <code>
    * public class MyWeld extends Weld {
    *    @Override
    *    protected void createDeployment() {
    *       Deployment myDeployment = new MyDeployment();
    *       deployment.getServices().add(ResourceLoader.class, new OSGIResourceLoader());
    *    }
    * }
    * </code>
    */
   private Deployment createDeployment()
   {
      NewSEWeldDeployment deployment = new NewSEWeldDeployment();
      deployment.getServices().add(ResourceLoader.class, new DefaultResourceLoader());
      return deployment;
   }

   /**
    * Shuts down Weld.
    */
   public void shutdown()
   {
      WeldManagerUtils.getInstanceByType(manager, ShutdownManager.class).shutdown();
   }
}
