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
package org.jboss.weld.tests.unit.deployment.structure.nonTransitiveResolution;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.AbstractMockDeployment;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockDeployment;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.test.Utils;
import org.testng.annotations.Test;

public class TransitiveResolutionTest
{

   @Test(description = "WELD-319")
   public void testBeansXmlIsolation()
   {
      MockBeanDeploymentArchive jar1 = new MockBeanDeploymentArchive("first-jar", Alt.class);
      MockBeanDeploymentArchive jar2 = new MockBeanDeploymentArchive("second-jar", Alt.class);
      jar1.getBeansXml().add(getClass().getResource("beans.xml"));
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war");
      war.getBeanDeploymentArchives().add(jar1);
      war.getBeanDeploymentArchives().add(jar2);
      
      Deployment deployment = new MockDeployment(war);
      
      TestContainer container = null;
      try
      {
         container = new TestContainer(new MockServletLifecycle(deployment, war)).startContainer().ensureRequestActive();
         BeanManagerImpl warBeanManager = container.getBeanManager();
         BeanManagerImpl jar1BeanManager = container.getLifecycle().getBootstrap().getManager(jar1);         
         BeanManagerImpl jar2BeanManager = container.getLifecycle().getBootstrap().getManager(jar2);
         assert warBeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
         assert !jar1BeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
         assert jar2BeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
      }
      finally
      {
         if (container != null)
         {
            container.stopContainer();
         }
      }
   }
   
   @Test(description = "WELD-319")
   public void testBeansXmlMultipleEnabling()
   {
      MockBeanDeploymentArchive jar1 = new MockBeanDeploymentArchive("first-jar", Alt.class);
      MockBeanDeploymentArchive jar2 = new MockBeanDeploymentArchive("second-jar", Alt.class);
      jar1.getBeansXml().add(getClass().getResource("beans.xml"));
      jar2.getBeansXml().add(getClass().getResource("beans.xml"));
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war");
      war.getBeanDeploymentArchives().add(jar1);
      war.getBeanDeploymentArchives().add(jar2);
      
      Deployment deployment = new MockDeployment(war);
      
      TestContainer container = null;
      try
      {
         container = new TestContainer(new MockServletLifecycle(deployment, war)).startContainer().ensureRequestActive();
         BeanManagerImpl warBeanManager = container.getBeanManager();
         BeanManagerImpl jar1BeanManager = container.getLifecycle().getBootstrap().getManager(jar1);         
         BeanManagerImpl jar2BeanManager = container.getLifecycle().getBootstrap().getManager(jar2);
         assert warBeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
         assert !jar1BeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
         assert !jar2BeanManager.getEnabledClasses().getAlternativeClasses().isEmpty();
      }
      finally
      {
         if (container != null)
         {
            container.stopContainer();
         }
      }
   }   

   @Test(description = "WELD-236")
   public void testTypicalEarStructure()
   {

      // Create the BDA in which we will deploy Foo. This is equivalent to a ejb
      // jar
      final MockBeanDeploymentArchive ejbJar = new MockBeanDeploymentArchive("ejb-jar", Foo.class);

      // Create the BDA in which we will deploy Bar. This is equivalent to a war
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war", Bar.class);

      // The war can access the ejb jar
      war.getBeanDeploymentArchives().add(ejbJar);

      // Create a deployment, any other classes are put into the ejb-jar (not
      // relevant to test)
      Deployment deployment = new AbstractMockDeployment(war, ejbJar)
      {

         public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass)
         {
            return ejbJar;
         }

      };

      TestContainer container = new TestContainer(new MockServletLifecycle(deployment, war));
      container.startContainer();
      container.ensureRequestActive();

      // Get the bean manager for war and ejb jar
      BeanManagerImpl warBeanManager = container.getBeanManager();
      BeanManagerImpl ejbJarBeanManager = container.getLifecycle().getBootstrap().getManager(ejbJar);

      assert warBeanManager.getBeans(Bar.class).size() == 1;
      assert warBeanManager.getBeans(Foo.class).size() == 1;
      assert ejbJarBeanManager.getBeans(Foo.class).size() == 1;
      assert ejbJarBeanManager.getBeans(Bar.class).size() == 0;
      Bar bar = Utils.getReference(warBeanManager, Bar.class);
      assert bar.getFoo() != null;
      assert bar.getBeanManager() != null;
      assert bar.getBeanManager().equals(warBeanManager);
      assert bar.getFoo().getBeanManager().equals(ejbJarBeanManager);
   }

}
