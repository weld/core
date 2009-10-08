package org.jboss.webbeans.bootstrap.api.test;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.ejb.api.SessionObjectReference;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.ejb.spi.InterceptorBindings;

public class MockEjbServices extends MockService implements EjbServices
{

   public Iterable<EjbDescriptor<?>> discoverEjbs()
   {
      return null;
   }

   public SessionObjectReference resolveEjb(EjbDescriptor<?> ejbDescriptor)
   {
      return null;
   }

   public void registerInterceptors(EjbDescriptor<?> ejbDescriptor, InterceptorBindings interceptorBindings)
   {
      // do nothing
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
