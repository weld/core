package org.jboss.jsr299.tck.tests.inheritance.realization;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

abstract class AbstractHorse
{
   
   @Produces @Smelly @RequestScoped @FarmAnimalDeploymentType private HorseDung dung = new HorseDung();
   
}
