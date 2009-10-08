package org.jboss.weld.injection.spi.helpers;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.injection.spi.ResourceInjectionServices;

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
