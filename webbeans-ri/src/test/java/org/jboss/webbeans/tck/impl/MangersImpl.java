package org.jboss.webbeans.tck.impl;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.webbeans.manager.Manager;

import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.tck.api.Managers;

public class MangersImpl implements Managers
{

   public Manager getManager()
   {
      return CurrentManager.rootManager();
   }

   public void setEnabledDeploymentTypes(List<Class<? extends Annotation>> enabledDeploymentTypes)
   {
      CurrentManager.rootManager().setEnabledDeploymentTypes(enabledDeploymentTypes);
   }
   
}
