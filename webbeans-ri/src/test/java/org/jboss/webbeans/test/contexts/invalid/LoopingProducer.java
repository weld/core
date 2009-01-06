package org.jboss.webbeans.test.contexts.invalid;

import javax.webbeans.Current;
import javax.webbeans.Produces;

public class LoopingProducer
{
   @Current Violation violation;
   
   @Produces
   public Violation produceViolation(Violation violation) {
      return violation;
   }
   
   public void ping() {
      System.out.println(violation);
   }
}
