package org.jboss.webbeans.bootstrap.api.test;

import javax.jms.Destination;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.jboss.webbeans.messaging.spi.JmsServices;

public class MockJmsServices implements JmsServices
{

   public <T extends Destination> T resolveDestination(String jndiName, String mappedName)
   {
      return null;
   }
   
   public QueueConnectionFactory getQueueConnectionFactory() 
   {
      return null;
   }
   
   public TopicConnectionFactory getTopicConnectionFactory() 
   {
      return null;
   }
   

}
