package org.jboss.weld.test.activities;

import javax.enterprise.event.Observes;

class Fox
{
   
   private static boolean observed = false;
   
   public void observe(@Observes NightTime nighttime)
   {
      observed = true;
   }
   
   public static boolean isObserved()
   {
      return observed;
   }
   
   public static void setObserved(boolean observed)
   {
      Fox.observed = observed;
   }
   
}
