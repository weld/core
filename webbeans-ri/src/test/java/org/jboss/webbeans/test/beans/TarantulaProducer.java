package org.jboss.webbeans.test.beans;

import javax.webbeans.Produces;

import org.jboss.webbeans.test.annotations.Tame;

public class TarantulaProducer
{
   
   @Produces @Tame public Tarantula produceTameTarantula()
   {
      return new DefangedTarantula();
   }
   
}
