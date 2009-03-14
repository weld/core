package org.jboss.webbeans.test.unit.implementation.producer;

import javax.inject.Current;
import javax.inject.Produces;
import javax.inject.manager.InjectionPoint;
import javax.inject.manager.Manager;

class ManagerProducer
{
   
   @Current Manager manager;
   
   private static boolean injectionPointInjected;
   
   public static boolean isInjectionPointInjected()
   {
      return injectionPointInjected;
   }
   
   public static void setInjectionPointInjected(boolean injectionPointInjected)
   {
      ManagerProducer.injectionPointInjected = injectionPointInjected;
   }

   @Produces
   Integer create(InjectionPoint point)
   {
      injectionPointInjected = point != null;
      return 10;
   }

}
