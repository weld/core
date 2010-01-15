package org.jboss.weld.tests.extensions.annotatedType;

import javax.inject.Inject;

public class Laundry
{
   @Inject @FastWashingMachine
   public WashingMachine fastWashingMachine;
   
   @Inject @EcoFriendlyWashingMachine
   public WashingMachine ecoFriendlyWashingMachine;
}
