package org.jboss.weld.tests.beanDeployment.producers.singleProducerMethod;

import javax.enterprise.inject.Produces;

public class TarantulaProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
}
