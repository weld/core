package org.jboss.weld.tests.nonContextual;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class LogManager
{

   @Produces
   public Logger createLogger(InjectionPoint injectionPoint)
   {
      return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
   }

}
