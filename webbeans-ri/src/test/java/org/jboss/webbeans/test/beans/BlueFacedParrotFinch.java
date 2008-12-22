package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Fires;
import javax.webbeans.Observer;

public class BlueFacedParrotFinch
{
   @Fires
   private Event<String> simpleEvent;

   public void methodThatFiresEvent()
   {
      simpleEvent.fire("An event");
   }

   public void methodThatRegistersObserver()
   {
      simpleEvent.observe(new Observer<String>()
      {
         public void notify(String event)
         {
         }
      });
   }
}
