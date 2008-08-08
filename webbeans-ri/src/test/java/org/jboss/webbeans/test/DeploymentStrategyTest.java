package org.jboss.webbeans.test;

import java.net.URL;

import javax.webbeans.Container;

import org.jboss.webbeans.ContainerImpl;
import org.jboss.webbeans.deployment.DeploymentStrategy;
import org.junit.Test;
import org.scannotation.ClasspathUrlFinder;

public class DeploymentStrategyTest
{

   @Test
   public void testDeploymentStrategy()
   {
      URL[] urls = {ClasspathUrlFinder.findClassBase(DeploymentStrategyTest.class)};
      Container container = new ContainerImpl(null);
      DeploymentStrategy deploymentStrategy = new DeploymentStrategy(Thread.currentThread().getContextClassLoader(), container);
      deploymentStrategy.scan(urls);
   }
   
}
