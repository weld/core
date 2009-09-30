package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;

import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Seat;

public class DriversSeatProducer
{

   @Produces @Drivers
   public Seat produceDriversSeat(DriversSeat seat)
   {
      return seat;
   }
   
}
