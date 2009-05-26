package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Current;

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
