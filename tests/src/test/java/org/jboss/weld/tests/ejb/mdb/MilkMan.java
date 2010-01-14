package org.jboss.weld.tests.ejb.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(activationConfig={
      @ActivationConfigProperty(propertyName="destinationType", propertyValue="javax.jms.Queue"),
      @ActivationConfigProperty(propertyName="destination",     propertyValue="queue/DLQ")
  })
public class MilkMan implements MessageListener
{

   public void onMessage(Message message)
   {
      
   }

}
