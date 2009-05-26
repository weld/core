package org.jboss.webbeans.bootstrap.api.test;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.ejb.api.SessionObjectReference;
import org.jboss.webbeans.ejb.spi.EjbDescriptor;
import org.jboss.webbeans.ejb.spi.EjbServices;

public class MockEjbServices implements EjbServices
{
   
   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return null;
   }
   
   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return null;
   }
   
   public Object resolveEjb(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public Object resolveRemoteEjb(String jndiName, String mappedName, String ejbLink)
   {
      return null;
   }
   
}
