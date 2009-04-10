package org.jboss.webbeans.resources.spi.helpers;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.resources.spi.ResourceServices;

public abstract class ForwardingResourceServices implements ResourceServices
{
   
   protected abstract ResourceServices delegate();

   public Object resolveResource(InjectionPoint injectionPoint)
   {
      return delegate().resolveResource(injectionPoint);
   }

   public Object resolveResource(String jndiName, String mappedName)
   {
      return delegate().resolveResource(jndiName, mappedName);
   }

}
