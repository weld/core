package org.jboss.weld.test.producer.method;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

class ManagerProducer
{
   
   @Inject BeanManager beanManager;
   
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
