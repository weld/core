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
import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;

public abstract class AbstractDeployment implements Deployment
{

   private final Collection<BeanDeploymentArchive> beanDeploymentArchives;
   private final ServiceRegistry services;
   private final Iterable<Metadata<Extension>> extensions;

   public AbstractDeployment(Collection<BeanDeploymentArchive> beanDeploymentArchives, Iterable<Metadata<Extension>> extensions)
   {
      this.services = new SimpleServiceRegistry();
      this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>(beanDeploymentArchives);
      this.extensions = extensions;
      configureServices();
   }
   
   public AbstractDeployment(BeanDeploymentArchive... beanDeploymentArchives)
   {
      this(asList(beanDeploymentArchives), ServiceLoader.load(Extension.class));
   }
   
   public AbstractDeployment(BeanDeploymentArchive beanDeploymentArchive, Extension... extensions)
   {
      this(singleton(beanDeploymentArchive), transform(extensions));
   }
   
   protected void configureServices()
   {
      services.add(TransactionServices.class, new MockTransactionServices());
      services.add(SecurityServices.class, new MockSecurityServices());
      services.add(ValidationServices.class, new MockValidationServices());
      services.add(EjbServices.class, new MockEjBServices());
      services.add(ResourceLoader.class, new MockResourceLoader());
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives;
   }

   public ServiceRegistry getServices()
   {
      return services;
   }
   
   public Iterable<Metadata<Extension>> getExtensions()
   {
      return extensions;
   }
   
   public static Iterable<Metadata<Extension>> transform(Extension... extensions)
   {
      List<Metadata<Extension>> result = new ArrayList<Metadata<Extension>>();
      for (final Extension extension : extensions)
      {
         result.add(new Metadata<Extension>()
         {
            
            public String getLocation()
            {
               return "unknown";
            }
            
            public Extension getValue()
            {
               return extension;
            }
            
         });
      }
      return result;
   }
   
}