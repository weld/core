package org.jboss.webbeans.bootstrap.api.test;

import java.util.Collection;

import javax.inject.manager.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.webbeans.persistence.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   public Collection<Class<?>> discoverEntities()
   {
      return null;
   }
   
   public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public EntityManager resolvePersistenceContext(String unitName)
   {
      return null;
   }
   
   public EntityManagerFactory resolvePersistenceUnit(String unitName)
   {
      return null;
   }
   
}
