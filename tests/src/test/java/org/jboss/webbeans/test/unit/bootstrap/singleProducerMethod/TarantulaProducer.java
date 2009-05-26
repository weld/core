package org.jboss.webbeans.test.unit.bootstrap.singleProducerMethod;

import javax.enterprise.inject.Produces;

public class TarantulaProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
}
