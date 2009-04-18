package org.jboss.jsr299.tck.tests.implementation.enterprise.lifecycle;

import javax.annotation.PreDestroy;
import javax.context.Dependent;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.inject.Current;

@Stateful
@Dependent
public class LandgraffenSchloss implements Schloss
{
   @Current
   private GrossStadt biggerCity;

   @PreDestroy
   public void destructionCallback()
   {
      biggerCity.schlossDestroyed();
   }

   @Remove
   public void remove()
   {
   }

}
