package org.jboss.weld.examples.pastecode.session;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class LogManager
{
   
   @Produces
   public Logger getLogger(InjectionPoint ip)
   {
      String category = ip.getMember().getDeclaringClass().getName();
      return Logger.getLogger(category);
   }

}
