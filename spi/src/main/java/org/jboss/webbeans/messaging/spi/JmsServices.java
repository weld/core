package org.jboss.webbeans.messaging.spi;


import javax.jms.Destination;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * A container should implement this interface to allow Web Beans to resolve Jms
 * Services
 * 
 * @author Pete Muir
 * 
 */
public interface JmsServices extends Service
{
   
   /**
    * Resolve the destination for the given JNDI name and mapped name
    * 
    * @param injectionPoint
    *           the injection point metadata
    * @return an instance of the resource
    * @throws IllegalStateException
    *            if no resource can be resolved for injection
    */
   public <T extends Destination> T resolveDestination(String jndiName, String mappedName);
   
   public QueueConnectionFactory getQueueConnectionFactory();
   
   public TopicConnectionFactory getTopicConnectionFactory();
   
}