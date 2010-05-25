/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.weld.environment.se.discovery;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbDescriptor;

/**
 * A deployment archive to registering classes and resources found in bean
 * archives on the classpath.
 * 
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class WeldSEBeanDeploymentArchive implements BeanDeploymentArchive
{

   private final Collection<Class<?>> weldClasses;
   private final Collection<URL> weldUrls;
   private final ServiceRegistry serviceRegistry;
   private final List<BeanDeploymentArchive> beanDeploymentArchives;
   private final String id;

   public WeldSEBeanDeploymentArchive(String id)
   {
      this.id = id;
      this.weldClasses = new HashSet<Class<?>>();
      this.weldUrls = new HashSet<URL>();
      this.serviceRegistry = new SimpleServiceRegistry();
      this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
   }

   /**
    * This is an alias for getBeansXml(), to make adding resources other than
    * beans.xml more natural.
    */
   public Collection<URL> getUrls()
   {
      return weldUrls;
   }

   public Collection<Class<?>> getBeanClasses()
   {
      return weldClasses;
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return this.beanDeploymentArchives;
   }

   public Collection<URL> getBeansXml()
   {
      return weldUrls;
   }

   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return Collections.EMPTY_SET;
   }

   public String getId()
   {
      return this.id;
   }

   public ServiceRegistry getServices()
   {
      return this.serviceRegistry;
   }
}
