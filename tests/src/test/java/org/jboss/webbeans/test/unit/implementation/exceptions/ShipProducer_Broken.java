package org.jboss.webbeans.test.unit.implementation.exceptions;

import javax.enterprise.inject.Produces;

public class ShipProducer_Broken
{
   
   @Produces @Large
   public Ship produceShip() throws FooException
   {
      throw new FooException();
   }
   
}
