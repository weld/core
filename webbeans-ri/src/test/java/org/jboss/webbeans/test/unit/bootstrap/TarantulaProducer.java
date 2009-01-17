package org.jboss.webbeans.test.unit.bootstrap;

import javax.webbeans.Produces;

public class TarantulaProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
}
