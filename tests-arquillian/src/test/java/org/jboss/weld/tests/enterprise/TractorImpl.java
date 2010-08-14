package org.jboss.weld.tests.enterprise;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

@RequestScoped @Stateful
public class TractorImpl implements Tractor, FarmMachine
{
   
   private Fuel observedFuel;
   
   public void observeRefuel(@Observes Fuel fuel)
   {
      this.observedFuel = fuel;
   }

   public Fuel getObservedFuel()
   {
      return observedFuel;
   }
   
   @Produces
   public Fumes smoke()
   {
      return new Fumes(10);
   }
   
   public void carbonCaptureDevice(@Disposes Fumes fumes)
   {
      fumes.setVolume(fumes.getVolume() - 5);
   }

}
