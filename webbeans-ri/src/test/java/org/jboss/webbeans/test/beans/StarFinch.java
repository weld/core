package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Named;
import javax.webbeans.Fires;
import javax.webbeans.Produces;
import javax.webbeans.RequestScoped;

@RequestScoped
@Named("StarFinch")
public class StarFinch
{
   public static class Mess
   {
   }

   public StarFinch()
   {
   }

   @Produces
   public Mess producerOfMesses(@Fires Event<Mess> messEvent)
   {
      Mess newMess = new Mess();
      messEvent.fire(newMess);
      return newMess;
   }
}
