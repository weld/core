/**
 * 
 */
package org.jboss.webbeans.mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.manager.InjectionPoint;
import javax.persistence.Entity;

import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.jpa.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   private final WebBeanDiscovery webBeanDiscovery;
   
   public MockJpaServices(WebBeanDiscovery webBeanDiscovery)
   {
      this.webBeanDiscovery = webBeanDiscovery;
   }
   
   public Object resolvePersistenceContext(InjectionPoint injectionPoint)
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