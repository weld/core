package org.jboss.weld.tests.unit.environments.servlet;

import javax.enterprise.inject.Produces;

public class TarantulaProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
}
