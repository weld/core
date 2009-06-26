package org.jboss.jsr299.tck.tests.inheritance.realization;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

abstract class AbstractCow
{
   
   @Produces @RequestScoped @Smelly @FarmAnimalDeploymentType CowDung getDung()
   {
      return new CowDung();
   }
   
}
