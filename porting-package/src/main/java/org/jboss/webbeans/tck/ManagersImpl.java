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

   public boolean isDefinitionError(Throwable throwable)
   {
      if (throwable == null)
      {
         return false;
      }
      else if (DefinitionException.class.isAssignableFrom(throwable.getClass()))
      {
         return true;
      }
      else
      {
         return isDefinitionError(throwable.getCause());
      }
   }
   
   public boolean isDeploymentError(Throwable throwable)
   {
      if (throwable == null)
      {
         return false;
      }
      else if (DeploymentException.class.isAssignableFrom(throwable.getClass()))
      {
         return true;
      }
      else
      {
         return isDeploymentError(throwable.getCause());
      }
   }
}
