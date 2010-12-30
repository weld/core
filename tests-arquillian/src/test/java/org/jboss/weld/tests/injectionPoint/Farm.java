package org.jboss.weld.tests.injectionPoint;

import java.io.Serializable;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

public class Farm implements Serializable
{
   
   @Inject
   private InjectionPoint injectionPoint;
   
   public void ping()
   {
   }
   
   public InjectionPoint getInjectionPoint()
   {
      return injectionPoint;
   }

}
