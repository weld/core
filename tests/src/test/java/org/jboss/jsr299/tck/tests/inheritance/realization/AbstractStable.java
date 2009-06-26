package org.jboss.jsr299.tck.tests.inheritance.realization;

import javax.enterprise.inject.Produces;

@AnotherDeploymentType
abstract class AbstractStable
{
   
   private @Produces @Cuddly Donkey donkey = new Donkey();
   
}
