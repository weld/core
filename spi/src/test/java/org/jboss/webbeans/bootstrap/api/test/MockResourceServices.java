package org.jboss.webbeans.bootstrap.api.test;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.resources.spi.ResourceServices;

public class MockResourceServices implements ResourceServices
{
   
   public Object resolveResource(InjectionPoint injectionPoint)
   {
      return null;
   }
   
}
