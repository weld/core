package org.jboss.jsr299.tck.tests.inheritance.realization;

import javax.enterprise.inject.Produces;

@AnotherDeploymentType
class AbstractKennel
{
 
   @Produces @Cuddly public Dog get()
   {
      return new Dog() {};
   }
   
   
}
