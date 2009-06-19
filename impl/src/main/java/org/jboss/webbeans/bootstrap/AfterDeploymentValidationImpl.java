package org.jboss.webbeans.bootstrap;

import javax.enterprise.inject.spi.AfterDeploymentValidation;

import org.jboss.webbeans.DeploymentException;

public class AfterDeploymentValidationImpl implements AfterDeploymentValidation
{
   public void addDeploymentProblem(Throwable t)
   {
      //XXX spec says need to delay abort until all observers
      //have been notified
      throw new DeploymentException(t);
   }

}
