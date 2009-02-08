package org.jboss.webbeans.tck;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.manager.Manager;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.webbeans.CurrentManager;

public class ManagersImpl implements Managers
{
   
   public Manager getManager()
   {
      return CurrentManager.rootManager();
   }
   
   public void setEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      CurrentManager.rootManager().setEnabledDeploymentTypes(enabledDeploymentTypes);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return CurrentManager.rootManager().getEnabledDeploymentTypes();
   }
   
}
