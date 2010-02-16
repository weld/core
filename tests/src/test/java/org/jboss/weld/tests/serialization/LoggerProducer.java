package org.jboss.weld.tests.serialization;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;

public class LoggerProducer
{

   @Produces
   public Logger produceLogger()
   {
      return Logger.getLogger("foo");
   }
   
}
