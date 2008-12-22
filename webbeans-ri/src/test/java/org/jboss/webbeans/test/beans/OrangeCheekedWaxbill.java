package org.jboss.webbeans.test.beans;

import javax.webbeans.Event;
import javax.webbeans.Initializer;
import javax.webbeans.Observable;
import javax.webbeans.RequestScoped;

import org.jboss.webbeans.test.beans.StarFinch.Mess;

@RequestScoped
public class OrangeCheekedWaxbill
{

   private Mess someMess;

   public OrangeCheekedWaxbill()
   {
   }

   @Initializer
   public void theInitializerMethod(@Observable Event<Mess> eventObject)
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
