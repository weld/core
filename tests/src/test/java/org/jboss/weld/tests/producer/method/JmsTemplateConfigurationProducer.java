package org.jboss.weld.tests.producer.method;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class JmsTemplateConfigurationProducer
{
   
   public static final int LONG_RECEIVE_TIMEOUT = 3 * 3600;
   public static final int SHORT_RECEIVE_TIMEOUT = 100;

   @Produces @Long
   private int longReceiveTimeout = LONG_RECEIVE_TIMEOUT;
   
   @Produces @Short
   private int shortReceiveTimeout = SHORT_RECEIVE_TIMEOUT;

   @Produces
   @Named
   public JmsTemplate getErrorQueueTemplate(@Long int receiveTimeout)
   {
      return new JmsTemplate(receiveTimeout);
   }

   @Produces
   @Named
   public JmsTemplate getLogQueueTemplate(@Short int receiveTimeout)
   {
      return new JmsTemplate(receiveTimeout);
   }
}