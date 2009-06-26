package org.jboss.webbeans.test.unit.implementation.enterprise.lifecycle;

import javax.annotation.PreDestroy;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Current;

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
