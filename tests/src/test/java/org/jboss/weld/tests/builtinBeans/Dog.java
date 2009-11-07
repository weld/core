package org.jboss.weld.tests.builtinBeans;

import java.io.Serializable;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class Dog implements Serializable
{
   
   private static InjectionPoint injectionPoint;
   
   @Inject
   public Dog(InjectionPoint injectionPoint)
   {
      Dog.injectionPoint = injectionPoint;
   }
   
   public static void reset()
   {
      Dog.injectionPoint = null;
   }
   
   public static InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }

}
