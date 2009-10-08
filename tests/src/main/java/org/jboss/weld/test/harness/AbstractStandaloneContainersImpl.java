package org.jboss.weld.test.harness;

import java.net.URL;
import java.util.Collection;

import org.jboss.testharness.api.DeploymentException;
import org.jboss.testharness.spi.StandaloneContainers;
import org.jboss.weld.mock.MockServletLifecycle;
import org.jboss.weld.mock.TestContainer;
import org.jboss.weld.mock.TestContainer.Status;

public abstract class AbstractStandaloneContainersImpl implements StandaloneContainers
{

   private DeploymentException deploymentException;
   
   private TestContainer testContainer;

   public boolean deploy(Collection<Class<?>> classes, Collection<URL> beansXml)
   {
      this.testContainer = new TestContainer(newLifecycle(), classes, beansXml);
      
      Status status = testContainer.startContainerAndReturnStatus();
      if (!status.isSuccess())
      {
         this.deploymentException = new DeploymentException("Error deploying beans", status.getDeploymentException());
         return false;
      }
      else
      {
         testContainer.getLifecycle().beginSession();
         testContainer.getLifecycle().beginRequest();
         return true;
      }
   }

   protected abstract MockServletLifecycle newLifecycle();

   public void deploy(Collection<Class<?>> classes) throws DeploymentException
   {
      deploy(classes, null);
   }

   public void cleanup()
   {
      // Np-op

   }

   public void setup()
   {
      // No-op
   }

   public DeploymentException getDeploymentException()
   {
      return deploymentException;
   }

   public void undeploy()
   {
      testContainer.stopContainer();
      testContainer = null;
      deploymentException = null;
   }

}