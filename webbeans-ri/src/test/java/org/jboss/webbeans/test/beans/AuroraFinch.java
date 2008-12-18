package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Initializer;
import javax.webbeans.Observable;
import org.jboss.webbeans.test.beans.StarFinch.Mess;

public class AuroraFinch
{

   private Mess someMess;

   @Initializer
   public AuroraFinch(@Observable Event<Mess> eventObject)
   {
      // Create a new mess and fire the event for it
      someMess = new Mess();
      eventObject.fire(someMess);
   }

   public Mess getSomeMess()
   {
      return someMess;
   }
}
