package org.jboss.webbeans.injection.spi.helpers;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.injection.spi.ResourceInjectionServices;

public abstract class ForwardingResourceInjectionServices implements ResourceInjectionServices
{
   
   protected abstract ResourceInjectionServices delegate();

   public Object resolveResource(InjectionPoint injectionPoint)
   {
      return delegate().resolveResource(injectionPoint);
   }

   public Object resolveResource(String jndiName, String mappedName)
   {
      return delegate().resolveResource(jndiName, mappedName);
   }

}
