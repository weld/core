package org.jboss.webbeans.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.AfterDeploymentValidation;

public class AfterDeploymentValidationImpl implements AfterDeploymentValidation
{
   private List<Throwable> deploymentProblems = new ArrayList<Throwable>();
   
   public void addDeploymentProblem(Throwable t)
   {
      deploymentProblems.add(t);
   }

   public List<Throwable> getDeploymentProblems()
   {
      return deploymentProblems;
   }
}
