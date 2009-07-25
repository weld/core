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

import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.persistence.spi.JpaServices;

public class MockJpaServices implements JpaServices
{
   
   private final Deployment deployment;
   
   public MockJpaServices(Deployment deployment)
   {
      this.deployment = deployment;
   }
   
   public EntityManager resolvePersistenceContext(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public EntityManagerFactory resolvePersistenceUnit(InjectionPoint injectionPoint)
   {
      return null;
   }
   
   public Collection<Class<?>> discoverEntities()
   {
      Set<Class<?>> classes = new HashSet<Class<?>>();
      for (BeanDeploymentArchive archive : deployment.getBeanDeploymentArchives())
      {
         discoverEntities(archive, classes);
      }
      return classes;
   }
   
   private void discoverEntities(BeanDeploymentArchive archive, Set<Class<?>> classes)
   {
      for (Class<?> clazz : archive.getBeanClasses())
      {
         if (clazz.isAnnotationPresent(Entity.class))
         {
            classes.add(clazz);
         }
      }
      for (BeanDeploymentArchive child : archive.getBeanDeploymentArchives())
      {
         discoverEntities(child, classes);
      }
   }

}