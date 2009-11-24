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

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.weld.manager.api.WeldManager;
import org.jboss.weld.environment.se.discovery.SEWeldDeployment;
import org.jboss.weld.environment.se.events.ContainerInitialized;
import org.jboss.weld.environment.se.util.Reflections;
import org.jboss.weld.environment.se.util.WeldManagerUtils;

/**
 * This is the main class that should always be called from the command line for
 * a Weld SE app. Something like: <code>
 * java -jar MyApp.jar org.jboss.weld.environment.se.StarMain arguments
 * </code>
 * 
 * @author Peter Royle
 * @author Pete Muir
 */
public class StartMain
{

   private static final String BOOTSTRAP_IMPL_CLASS_NAME = "org.jboss.weld.bootstrap.WeldBootstrap";
   private final Bootstrap bootstrap;
   private final BeanStore applicationBeanStore;
   public static String[] PARAMETERS;
   private WeldManager manager;

   public StartMain(String[] commandLineArgs)
   {
      PARAMETERS = commandLineArgs;
      try
      {
         bootstrap = Reflections.newInstance(BOOTSTRAP_IMPL_CLASS_NAME, Bootstrap.class);
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Error loading Weld bootstrap, check that Weld is on the classpath", e);
      }
      this.applicationBeanStore = new ConcurrentHashMapBeanStore();
   }

   public BeanManager go()
   {
      SEWeldDeployment deployment = new SEWeldDeployment()
      {
      };
      bootstrap.startContainer(Environments.SE, deployment, this.applicationBeanStore);
      final BeanDeploymentArchive mainBeanDepArch = deployment.getBeanDeploymentArchives().get(0);
      this.manager = bootstrap.getManager(mainBeanDepArch);
      bootstrap.startInitialization();
      bootstrap.deployBeans();
      WeldManagerUtils.getInstanceByType(manager, ShutdownManager.class).setBootstrap(bootstrap);
      bootstrap.validateBeans();
      bootstrap.endInitialization();

      this.manager.fireEvent(new ContainerInitialized());
      return this.manager;
   }

   /**
    * The main method called from the command line.
    * 
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      new StartMain(args).go();
   }

   public static String[] getParameters()
   {
      // TODO(PR): make immutable
      return PARAMETERS;
   }

}
