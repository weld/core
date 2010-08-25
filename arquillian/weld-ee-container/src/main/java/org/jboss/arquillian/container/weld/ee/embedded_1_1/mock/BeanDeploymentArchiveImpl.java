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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.jboss.weld.bootstrap.spi.BeansXml.EMPTY_BEANS_XML;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.EnterpriseBean;
import javax.ejb.MessageDriven;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;

/**
 * @author pmuir
 *
 */
public class BeanDeploymentArchiveImpl implements BeanDeploymentArchive
{  
   
   private final Collection<Class<?>> beanClasses;
   private final BeansXml beansXml;
   private final List<EjbDescriptor<?>> ejbs;
   private final ServiceRegistry services;
   private final Collection<BeanDeploymentArchive> bdas;
   private final String id;
   
   public BeanDeploymentArchiveImpl(BeansXml beansXml, Collection<Class<?>> classes)
   {
      this("test", beansXml, classes);
   }
   
   public BeanDeploymentArchiveImpl(Collection<Class<?>> classes)
   {
      this("test", EMPTY_BEANS_XML, classes);
   }
   
   public BeanDeploymentArchiveImpl(String id, Class<?>... classes)
   {
      this(id, EMPTY_BEANS_XML, asList(classes));
   }
   
   public BeanDeploymentArchiveImpl(String id, BeansXml beansXml, Class<?>... classes)
   {
      this(id, beansXml, asList(classes));
   }
   
   public BeanDeploymentArchiveImpl(String id, BeansXml beansXml, Collection<Class<?>> beanClasses)
   {
      this.services = new SimpleServiceRegistry();
      configureServices();
      this.bdas = new HashSet<BeanDeploymentArchive>();
      this.beanClasses = beanClasses;
      this.beansXml = beansXml;
      ejbs = new ArrayList<EjbDescriptor<?>>();
      for (Class<?> ejbClass : discoverEjbs(getBeanClasses()))
      {
         ejbs.add(MockEjbDescriptor.of(ejbClass));
      }
      this.id = id;
   }
   
   protected void configureServices()
   {
      this.services.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      this.services.add(JpaInjectionServices.class, new MockJpaInjectionServices());
      this.services.add(ResourceInjectionServices.class, new MockResourceInjectionServices());
   }

   public Collection<Class<?>> getBeanClasses()
   {
      return beanClasses;
   }

   public BeansXml getBeansXml()
   {
      return beansXml;
   }
   
   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return bdas;
   }
   
   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return ejbs;
   }
   
   protected static Iterable<Class<?>> discoverEjbs(Iterable<Class<?>> webBeanClasses)
   {
      Set<Class<?>> ejbs = new HashSet<Class<?>>();
      for (Class<?> clazz : webBeanClasses)
      {
         if (clazz.isAnnotationPresent(Stateless.class) || clazz.isAnnotationPresent(Stateful.class) || clazz.isAnnotationPresent(MessageDriven.class) || clazz.isAnnotationPresent(Singleton.class) || EnterpriseBean.class.isAssignableFrom(clazz)) 
         {
            ejbs.add(clazz);
         }
      }
      return ejbs;
   }
   
   public ServiceRegistry getServices()
   {
      return services;
   }
   
   public String getId()
   {
      return id;
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof BeanDeploymentArchiveImpl)
      {
         BeanDeploymentArchiveImpl that = (BeanDeploymentArchiveImpl) obj;
         return this.getId().equals(that.getId());
      }
      else
      {
         return false;
      }
   }
   
   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

}
