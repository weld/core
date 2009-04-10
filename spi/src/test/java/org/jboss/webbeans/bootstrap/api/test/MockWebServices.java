package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.ws.spi.WebServices;

public class MockWebServices implements WebServices
{

   public Object resolveResource(String jndiName, String mappedName)
   {
      return null;
   }

}
