package org.jboss.webbeans.bootstrap.spi;

import org.jboss.webbeans.ejb.spi.EjbDescriptor;

public abstract class ForwardingEjbDiscovery implements EjbDiscovery
{
   
   protected abstract EjbDiscovery delegate();
   
   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return delegate().discoverEjbs();
   }
   
}
