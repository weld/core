package org.jboss.weld.tests.enterprise;

import javax.ejb.Local;

@Local
public interface FarmMachine
{

   public Fuel getObservedFuel();

}