package org.jboss.webbeans.test.beans;

import javax.jms.Message;
import javax.webbeans.Production;

@Production
//@MessageDriven
public class Leopard implements javax.jms.MessageListener
{

   public void onMessage(Message message)
   {
      // TODO Auto-generated method stub
      
   }

}
