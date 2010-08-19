package org.jboss.weld.tests.enterprise.lifecycle;

import javax.enterprise.inject.Produces;

public class ChickenHutchFactory
{
   
   // Redirect via a producer method so dependents are automatically destroyed
   @Produces @MassProduced
   public ChickenHutch makeChickenHutch(ChickenHutch chickenHutch)
   {
      chickenHutch.ping();
      return chickenHutch;
   }

}
