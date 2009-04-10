package org.jboss.webbeans.messaging.spi.helpers;

import org.jboss.webbeans.ws.spi.WebServices;

public abstract class ForwardingJmsServices implements WebServices
{

   protected abstract WebServices delegate();
   
   public Object resolveResource(String jndiName, String mappedName)
   {
      return delegate().resolveResource(jndiName, mappedName);
   }

}
