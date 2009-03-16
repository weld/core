package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.context.ApplicationScoped;
import javax.inject.Current;

@ApplicationScoped
class Petrol
{
   
   public static boolean success;
   
   @Current Car car;
   
   public Petrol()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (car.getName().equals("herbie"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "petrol";
   }
   
}
