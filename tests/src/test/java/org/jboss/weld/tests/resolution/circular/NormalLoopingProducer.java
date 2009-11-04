package org.jboss.weld.tests.resolution.circular;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
class NormalLoopingProducer
{
   
   @Produces @ApplicationScoped @NormalLooping
   public Violation produceViolation(@NormalLooping Violation violation) {
      return new Violation();
   }

} 