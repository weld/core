package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.Observer;

public class BlueFacedParrotFinch
{
   @Observable
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
