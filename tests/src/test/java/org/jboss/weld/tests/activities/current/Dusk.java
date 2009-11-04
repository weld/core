package org.jboss.weld.tests.activities.current;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

class Dusk
{
   
   @Inject
   public Dusk(@Any Event<NightTime> event)
   {
      event.fire(new NightTime());
   }
   
   public void ping() {}
   
}
