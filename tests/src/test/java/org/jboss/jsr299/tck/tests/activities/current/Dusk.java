package org.jboss.jsr299.tck.tests.activities.current;

import javax.enterprise.inject.Initializer;
import javax.event.Event;
import javax.enterprise.inject.Any;

class Dusk
{
   
   @Initializer
   public Dusk(@Any Event<NightTime> event)
   {
      event.fire(new NightTime());
   }
   
   public void ping() {}
   
}
