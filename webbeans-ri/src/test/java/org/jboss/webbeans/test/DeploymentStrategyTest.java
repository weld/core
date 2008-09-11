package org.jboss.webbeans.test;

import java.net.URL;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.deployment.DeploymentStrategy;
import org.jboss.webbeans.scannotation.ClasspathUrlFinder;
import org.testng.annotations.Test;

public class DeploymentStrategyTest
{

   @Test(groups="deployment")
   public void testDeploymentStrategy()
   {
      URL[] urls = {ClasspathUrlFinder.findClassBase(DeploymentStrategyTest.class)};
      ContainerImpl container = new ContainerImpl(null);
      DeploymentStrategy deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      deploymentStrategy.scan(urls, "org.jboss.webbeans.test.annotations.broken", "org.jboss.webbeans.test.bindings.broken", "org.jboss.webbeans.test.components.broken");
   }
   
}
