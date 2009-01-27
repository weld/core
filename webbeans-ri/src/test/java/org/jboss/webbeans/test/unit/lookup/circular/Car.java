package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.inject.Current;

class Car
{
   
   public static boolean success;
   
   @Current Petrol petrol;
   
   public Car()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (petrol.getName().equals("petrol"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "herbie";
   }
   
}
