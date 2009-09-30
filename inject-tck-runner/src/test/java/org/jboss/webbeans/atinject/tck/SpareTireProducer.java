package org.jboss.webbeans.atinject.tck;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.accessories.SpareTire;

public class SpareTireProducer
{

   @Produces @Named("spare")
   public Tire produceSpareTire(SpareTire spareTire)
   {
      return spareTire;
   }
   
}
