package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class StringGenerator
{

   private static InjectionPoint injectionPoint;
   
   public static InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }
   
   public static void reset()
   {
      injectionPoint = null;
   }
   
   @Produces String getString(InjectionPoint ip)
   {
      injectionPoint = ip;
      return "";
   }
   
}
