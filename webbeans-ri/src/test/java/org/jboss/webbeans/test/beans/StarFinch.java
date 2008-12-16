package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Observable;
import javax.webbeans.Produces;
import javax.webbeans.RequestScoped;

@RequestScoped
public class StarFinch
{
   public static class Mess
   {
   }

   public StarFinch()
   {
   }

   @Produces
   public Mess producerOfMesses(@Observable Event<Mess> messEvent)
   {
      Mess newMess = new Mess();
      messEvent.fire(newMess);
      return newMess;
   }
}
