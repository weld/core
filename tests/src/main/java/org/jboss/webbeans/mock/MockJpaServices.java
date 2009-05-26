/**
 * 
 */
package org.jboss.webbeans.mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.persistence.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   private final WebBeanDiscovery webBeanDiscovery;
   
   public MockJpaServices(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
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
   
   public Collection<Class<?>> discoverEntities()
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (Class<?> clazz : webBeanDiscovery.discoverWebBeanClasses())
      {
         if (clazz.isAnnotationPresent(Entity.class))
         {
            classes.add(clazz);
         }
      }
      return classes;
   }

}