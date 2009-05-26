package org.jboss.webbeans.bootstrap.api.test;

import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.webbeans.resources.spi.ResourceServices;

public class MockResourceServices implements ResourceServices
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
