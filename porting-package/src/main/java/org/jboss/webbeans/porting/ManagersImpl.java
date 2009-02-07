package org.jboss.webbeans.porting;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.manager.Manager;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.tck.spi.Managers;

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
