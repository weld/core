package org.jboss.weld.test.resolution.circular;

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