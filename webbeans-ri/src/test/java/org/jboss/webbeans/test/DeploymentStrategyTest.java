package org.jboss.webbeans.test;

import java.net.URL;

import javax.webbeans.Container;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.deployment.DeploymentStrategy;
import org.jboss.webbeans.scannotation.ClasspathUrlFinder;
import org.junit.Test;

public class DeploymentStrategyTest
{

   @Test
   public void testDeploymentStrategy()
   {
      URL[] urls = {ClasspathUrlFinder.findClassBase(DeploymentStrategyTest.class)};
      Container container = new ContainerImpl(null);
      DeploymentStrategy deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      deploymentStrategy.scan(urls, "org.jboss.webbeans.test.annotations.broken", "org.jboss.webbeans.test.bindings.broken", "org.jboss.webbeans.test.components.broken");
   }
   
}
