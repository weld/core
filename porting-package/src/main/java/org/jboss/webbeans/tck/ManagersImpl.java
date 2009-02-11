package org.jboss.webbeans.tck;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.inject.manager.Manager;

import org.jboss.jsr299.tck.spi.Managers;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.WebBean;

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
      List<Class<? extends Annotation>> deploymentTypes = new ArrayList<Class<? extends Annotation>>(CurrentManager.rootManager().getEnabledDeploymentTypes());
      deploymentTypes.remove(WebBean.class);
      return deploymentTypes;
   }
   
}
