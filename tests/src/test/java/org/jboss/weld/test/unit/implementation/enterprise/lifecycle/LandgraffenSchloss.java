package org.jboss.weld.test.unit.implementation.enterprise.lifecycle;

import javax.annotation.PreDestroy;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Stateful
@Dependent
public class LandgraffenSchloss implements Schloss
{
   @Inject
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
