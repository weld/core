package org.jboss.webbeans.test.beans.broken;

import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.Observer;

import org.jboss.webbeans.test.annotations.Role;
import org.jboss.webbeans.test.bindings.RoleBinding;

public class SweeWaxbill
{
   @Observable @Role("Admin")
   private Event<String> simpleEvent;

   public void methodThatFiresEvent()
   {
      simpleEvent.fire("An event", new RoleBinding("Admin"));
   }

   public void methodThatRegistersObserver()
   {
      simpleEvent.observe(new Observer<String>()
      {

         public void notify(String event)
         {
         }
      }, new RoleBinding("Admin"));
   }
}
