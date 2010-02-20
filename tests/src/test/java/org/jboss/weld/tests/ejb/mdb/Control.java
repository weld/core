package org.jboss.weld.tests.ejb.mdb;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Control
{
   
   private volatile boolean messageDelivered;

   public boolean isMessageDelivered()
   {
      return messageDelivered;
   }

   public void setMessageDelivered(boolean messageDelivered)
   {
      this.messageDelivered = messageDelivered;
   }

}
