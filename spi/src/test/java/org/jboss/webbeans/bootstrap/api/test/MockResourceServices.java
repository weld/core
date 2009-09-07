package org.jboss.webbeans.bootstrap.api.test;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.injection.spi.ResourceInjectionServices;

public class MockResourceServices extends MockService implements ResourceInjectionServices
{
   
   public Object resolveResource(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public Object resolveResource(String jndiName, String mappedName)
   {
      return null;
   }
   
}
