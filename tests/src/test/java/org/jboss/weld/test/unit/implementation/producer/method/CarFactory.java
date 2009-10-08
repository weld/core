package org.jboss.weld.test.unit.implementation.producer.method;

import javax.enterprise.inject.Produces;

public class CarFactory
{

   @Produces @Important
   public Car produceGovernmentCar()
   {
      return null;
   }
   
}
