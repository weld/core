package org.jboss.weld.test.unit.deployment.structure.nonTransitiveResolution;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.mock.AbstractMockDeployment;
import org.jboss.weld.mock.MockBeanDeploymentArchive;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.testng.annotations.Test;

public class NonTransitiveResolutionTest
{
   
   @Test(description="WELD-236")
   public void test()
   { 
      
      // Create the BDA in which we will deploy Foo. This is equivalent to a ejb jar
      final MockBeanDeploymentArchive ejbJar = new MockBeanDeploymentArchive("ejb-jar", Foo.class);
      
      // Create the BDA in which we will deploy Bar. This is equivalent to a war
      MockBeanDeploymentArchive war = new MockBeanDeploymentArchive("war", Bar.class);
      
      // The war can access the ejb jar
      war.getBeanDeploymentArchives().add(ejbJar);
      
      // Create a deployment, any other classes are put into the ejb-jar (not relevant to test)
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
      
      // Get the bean manager for bda1 and bda2
      BeanManagerImpl beanManager1 = container.getBeanManager();
   }
   
}
