package org.jboss.weld.tck;

import javax.enterprise.inject.UnproxyableResolutionException;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.weld.DefinitionException;
import org.jboss.weld.DeploymentException;
import org.jboss.weld.test.BeanManagerLocator;

public class ManagersImpl implements Managers
{
   
   public BeanManager getManager()
   {
      return BeanManagerLocator.INSTANCE.locate();
   }

   public boolean isDefinitionError(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDefinitionException(deploymentException.getCause());
   }

   private boolean isDefinitionException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DefinitionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDefinitionException(t.getCause());
      }
   }

   public boolean isDeploymentError(org.jboss.testharness.api.DeploymentException deploymentException)
   {
      return isDeploymentException(deploymentException.getCause());
   }

   public boolean isDeploymentException(Throwable t)
   {
      if (t == null)
      {
         return false;
      }
      else if (DeploymentException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else if (UnproxyableResolutionException.class.isAssignableFrom(t.getClass()))
      {
         return true;
      }
      else
      {
         return isDeploymentException(t.getCause());
      }
   }
}
