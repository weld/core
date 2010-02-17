package org.jboss.weld.tests.injectionPoint;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

@Special @ExtraSpecial
public class Pig
{
   
   @Inject InjectionPoint injectionPoint;
   
   public InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }
   
}
