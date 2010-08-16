/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.weld.ee.embedded_1_1.mock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.injection.spi.JpaInjectionServices;

public class MockJpaServices implements JpaInjectionServices
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
   
   public void cleanup() {}

}