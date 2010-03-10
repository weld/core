package org.jboss.weld.tests.ejb.mdb;

import javax.ejb.MessageDrivenContext;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Control
{
   private volatile boolean messageDelivered;
   private volatile boolean contextSet;

   public boolean isMessageDelivered()
   {
      return messageDelivered;
   }

   public void setMessageDelivered(boolean messageDelivered)
   {
      this.messageDelivered = messageDelivered;
   }

   public void setContext(MessageDrivenContext context)
   {
      if (context != null)
      {
         contextSet = true;
      }
   }

   public boolean isContextSet()
   {
      return contextSet;
   }

}
