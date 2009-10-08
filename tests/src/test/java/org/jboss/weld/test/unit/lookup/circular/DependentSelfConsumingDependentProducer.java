package org.jboss.weld.test.unit.lookup.circular;

import javax.enterprise.inject.Produces;

class DependentSelfConsumingDependentProducer
{
   @SelfConsumingDependent Violation violation;
   
   @Produces @SelfConsumingDependent
   public Violation produceViolation() {
      return new Violation();
   }
   
   public void ping() {
      
   }
} 