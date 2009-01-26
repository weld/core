package org.jboss.webbeans.test.tck;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.inject.manager.Manager;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.tck.api.Managers;
import org.jboss.webbeans.test.mock.MockBootstrap;

public class ManagersImpl implements Managers
{
   
   public Manager createManager(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      new MockBootstrap();
      if (enabledDeploymentTypes != null)
      {
         CurrentManager.rootManager().setEnabledDeploymentTypes(enabledDeploymentTypes);
      }
      return CurrentManager.rootManager();
   }
   
   public Manager createManager()
   {
      return createManager(null);
   }

   public List<Class<? extends Annotation>> getEnabledDeploymentTypes()
   {
      return CurrentManager.rootManager().getEnabledDeploymentTypes();
   }
   
}
