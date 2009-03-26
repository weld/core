package org.jboss.webbeans.bootstrap.api.test;

import java.util.Collection;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.jpa.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   public Collection<Class<?>> discoverEntities()
   {
      // TODO Auto-generated method stub
      return null;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      // TODO Auto-generated method stub
      return null;
   }
   
}
