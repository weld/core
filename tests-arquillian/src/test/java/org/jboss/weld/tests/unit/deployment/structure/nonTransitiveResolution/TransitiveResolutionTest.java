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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.weld.test.Utils.getReference;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.mock.AbstractMockDeployment;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockDeployment;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.test.Utils;
import org.jboss.weld.xml.BeansXmlImpl;
import org.junit.Assert;
import org.junit.Test;

public class TransitiveResolutionTest
{
   /*
    * description = "WELD-319"
    */
   @Test
   public void testBeansXmlIsolation()
   {
      MockBeanDeploymentArchive jar1 = new MockBeanDeploymentArchive("first-jar", Alt.class);
      MockBeanDeploymentArchive jar2 = new MockBeanDeploymentArchive("second-jar", Alt.class);
      jar1.setBeansXml(new BeansXmlImpl(Arrays.asList(Alt.class.getName()), Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList()));
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
         Assert.assertTrue(warBeanManager.getEnabled().getAlternativeClasses().isEmpty());
         Assert.assertFalse(jar1BeanManager.getEnabled().getAlternativeClasses().isEmpty());
         Assert.assertTrue(jar2BeanManager.getEnabled().getAlternativeClasses().isEmpty());
      }
      finally
      {
         if (container != null)
         {
            container.stopContainer();
         }
      }
   }
   
   /*
    * description = "WELD-319"
    */
   @Test
   public void testBeansXmlMultipleEnabling()
   {
      MockBeanDeploymentArchive jar1 = new MockBeanDeploymentArchive("first-jar", Alt.class);
      MockBeanDeploymentArchive jar2 = new MockBeanDeploymentArchive("second-jar", Alt.class);
      jar1.setBeansXml(new BeansXmlImpl(Arrays.asList(Alt.class.getName()), Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList()));
      jar2.setBeansXml(new BeansXmlImpl(Arrays.asList(Alt.class.getName()), Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList()));
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
         Assert.assertTrue(warBeanManager.getEnabled().getAlternativeClasses().isEmpty());
         Assert.assertFalse(jar1BeanManager.getEnabled().getAlternativeClasses().isEmpty());
         Assert.assertFalse(jar2BeanManager.getEnabled().getAlternativeClasses().isEmpty());
      }
      finally
      {
         if (container != null)
         {
            container.stopContainer();
         }
      }
   }   

   /*
    * description = "WELD-236"
    */
   @Test
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

      Assert.assertEquals(1, warBeanManager.getBeans(Bar.class).size());
      Assert.assertEquals(1, warBeanManager.getBeans(Foo.class).size());
      Assert.assertEquals(1, ejbJarBeanManager.getBeans(Foo.class).size());
      Assert.assertEquals(0, ejbJarBeanManager.getBeans(Bar.class).size());
      Bar bar = Utils.getReference(warBeanManager, Bar.class);
      Assert.assertNotNull(bar.getFoo());
      Assert.assertNotNull(bar.getBeanManager());
      Assert.assertEquals(warBeanManager, bar.getBeanManager());
      Assert.assertEquals(ejbJarBeanManager, bar.getFoo().getBeanManager());
   }
   
   /*
    * WELD-507
    */
   @Test
   public void testInterceptorEnabledInWarButPackagedInEjbJar()
   {

      // Create the BDA in which we will deploy Foo. This is equivalent to a ejb
      // jar
      final MockBeanDeploymentArchive ejbJar = new MockBeanDeploymentArchive("ejb-jar", Basic.class, BasicInterceptor.class, Simple.class);

      // Create the BDA in which we will deploy Bar. This is equivalent to a war
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war", Complex.class);
      war.setBeansXml(new BeansXml()
      {
         
         public List<String> getEnabledInterceptors()
         {
            return asList(BasicInterceptor.class.getName());
         }
         
         public List<String> getEnabledDecorators()
         {
            return emptyList();
         }
         
         public List<String> getEnabledAlternativeStereotypes()
         {
            return emptyList();
         }
         
         public List<String> getEnabledAlternativeClasses()
         {
            return emptyList();
         }
      });

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

      
      BasicInterceptor.reset();
      Simple simple = getReference(ejbJarBeanManager, Simple.class);
      simple.ping("14");
      assertNull(BasicInterceptor.getTarget());
      
      BasicInterceptor.reset();
      Complex complex = getReference(warBeanManager, Complex.class);
      complex.ping("14");
      assertNotNull(BasicInterceptor.getTarget());
      assertTrue(BasicInterceptor.getTarget() instanceof Complex);
      assertEquals("14", ((Complex) BasicInterceptor.getTarget()).getId());
   }
   
   /*
    * WELD-507
    */
   @Test
   public void testDecoratorEnabledInWarButPackagedInEjbJar()
   {

      // Create the BDA in which we will deploy Foo. This is equivalent to a ejb
      // jar
      final MockBeanDeploymentArchive ejbJar = new MockBeanDeploymentArchive("ejb-jar", Blah.class, BlahDecorator.class, BlahImpl.class);

      // Create the BDA in which we will deploy Bar. This is equivalent to a war
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war", BlahImpl2.class);
      war.setBeansXml(new BeansXml()
      {
         
         public List<String> getEnabledInterceptors()
         {
            return emptyList();
         }
         
         public List<String> getEnabledDecorators()
         {
            return asList(BlahDecorator.class.getName());
         }
         
         public List<String> getEnabledAlternativeStereotypes()
         {
            return emptyList();
         }
         
         public List<String> getEnabledAlternativeClasses()
         {
            return emptyList();
         }
      });

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

      
      BasicInterceptor.reset();
      Blah blah = getReference(ejbJarBeanManager, Blah.class);
      blah.ping(10);
      assertEquals(10, blah.getI());
      
      BasicInterceptor.reset();
      blah = getReference(warBeanManager, Blah.class, new AnnotationLiteral<Baz>() {});
      blah.ping(10);
      assertEquals(11, blah.getI());
   }

}
