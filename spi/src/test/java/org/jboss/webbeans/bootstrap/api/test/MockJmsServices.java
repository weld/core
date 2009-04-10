package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.messaging.spi.JmsServices;

public class MockJmsServices implements JmsServices
{

   public Object resolveDestination(String jndiName, String mappedName)
   {
      return null;
   }

}
