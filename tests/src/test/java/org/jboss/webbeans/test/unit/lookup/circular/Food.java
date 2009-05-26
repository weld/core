package org.jboss.webbeans.test.unit.lookup.circular;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Current;

@ApplicationScoped
class Food
{
   
   public static boolean success;
   
   @Current Pig pig;
   
   public Food()
   {
      success = false;
   }
   
   @PostConstruct
   public void postConstruct()
   {
      if (pig.getName().equals("john"))
      {
         success = true;
      }
   }
   
   public String getName()
   {
      return "food";
   }
   
}
