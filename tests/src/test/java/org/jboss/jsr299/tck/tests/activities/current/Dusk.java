package org.jboss.jsr299.tck.tests.activities.current;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Initializer;
import javax.event.Event;

class Dusk
{
   
   @Initializer
   public Dusk(@Any Event<NightTime> event)
   {
      event.fire(new NightTime());
   }
   
   public void ping() {}
   
}
