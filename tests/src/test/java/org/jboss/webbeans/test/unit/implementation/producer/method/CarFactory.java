package org.jboss.webbeans.test.unit.implementation.producer.method;

import javax.enterprise.inject.Produces;

public class CarFactory
{

   @Produces @Important
   public Car produceGovernmentCar()
   {
      return null;
   }
   
}
