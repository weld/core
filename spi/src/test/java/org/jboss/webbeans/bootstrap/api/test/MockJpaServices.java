package org.jboss.webbeans.bootstrap.api.test;

import java.util.Collection;

import javax.inject.manager.InjectionPoint;

import org.jboss.webbeans.jpa.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   public Collection<Class<?>> discoverEntities()
   {
      return null;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      return null;
   }
   
}
