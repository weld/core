package org.jboss.weld.tests.producer.method;

public class JmsTemplate
{
   
   private final int receiveTimeout;
   
   public JmsTemplate(int receiveTimeout)
   {
      this.receiveTimeout = receiveTimeout;
   }
   
   public int getReceiveTimeout()
   {
      return receiveTimeout;
   }

}
