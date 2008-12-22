package org.jboss.webbeans.test.beans;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.webbeans.ConversationScoped;
import javax.webbeans.Destructor;
import javax.webbeans.Event;
import javax.webbeans.Fires;

import org.jboss.webbeans.test.beans.StarFinch.Mess;

@ConversationScoped @Stateful
public class EuropeanGoldfinch
{
   private Mess someMess;

   public Mess getSomeMess()
   {
      return someMess;
   }

   @Destructor @Remove
   public void remove(@Fires Event<Mess> eventObject)
   {
      // Create a new mess and fire the event for it
      someMess = new Mess();
      eventObject.fire(someMess);
   }

}
