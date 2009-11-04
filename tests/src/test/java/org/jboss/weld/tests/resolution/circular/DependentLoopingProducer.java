package org.jboss.weld.tests.resolution.circular;

import javax.enterprise.inject.Produces;

class DependentLoopingProducer
{
   
   @Produces @DependentLooping
   public Violation produceViolation(@DependentLooping Violation violation) {
      return new Violation();
   }

} 