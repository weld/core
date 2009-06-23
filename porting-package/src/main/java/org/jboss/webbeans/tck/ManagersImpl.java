package org.jboss.webbeans.tck;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.DeploymentException;
import org.jboss.webbeans.WebBean;

public class ManagersImpl implements Managers
{

   public BeanManager getManager()
   {
      return CurrentManager.rootManager();
   }

   public void setEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      CurrentManager.rootManager().setEnabledDeploymentTypes(enabledDeploymentTypes);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      List<Class<? extends Annotation>> deploymentTypes = new ArrayList<Class<? extends Annotation>>(CurrentManager.rootManager().getEnabledDeploymentTypes());
      deploymentTypes.remove(WebBean.class);
      return deploymentTypes;
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
      else
      {
         return isDeploymentException(t.getCause());
      }
   }
}
