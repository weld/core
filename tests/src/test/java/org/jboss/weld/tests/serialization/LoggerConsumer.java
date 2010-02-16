package org.jboss.weld.tests.serialization;

import java.io.Serializable;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class LoggerConsumer implements Serializable
{
   
   @Inject Logger log;
   
   public void ping()
   {
      
   }

}
