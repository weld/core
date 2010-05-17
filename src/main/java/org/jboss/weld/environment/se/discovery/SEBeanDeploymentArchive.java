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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.spi.EjbDescriptor;

/**
 * A Java SE implementation of BeanDeploymentArchive. It is essentially an
 * adaptor from the SEWeldDiscovery to the BeanDeploymentArchive interface.
 * It returns, in a single logical archive, all Bean classes and beans.xml
 * descriptors. It always returns an empty collection of EJBs.
 * 
 * @author Peter Royle
 */
public class SEBeanDeploymentArchive implements BeanDeploymentArchive
{
   private final SEWeldDiscovery wbDiscovery;
   private final ServiceRegistry serviceRegistry;

   /**
    * @param deployment Used to gain access to the ResourceLoader, in case one is defined.
    */
   public SEBeanDeploymentArchive(SEWeldDiscovery discovery)
   {
      this.wbDiscovery = discovery;
      {
      };
      this.serviceRegistry = new SimpleServiceRegistry();
   }

   /**
    * @return a collection of all Bean classes on the classpath.
    */
   public Collection<Class<?>> getBeanClasses()
   {
      return wbDiscovery.getWbClasses();
   }

   /**
    * @return an empty collection, since this instance is the only logical
    *         archive for the current SE classloader.
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.EMPTY_LIST;
   }

   /**
    * @return all beans.xml decriptors found on the classpath.
    */
   public Collection<URL> getBeansXml()
   {
      return wbDiscovery.discoverWeldXml();
   }

   /**
    * @return an empty collection since there are no EJBs in Java SE.
    */
   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return Collections.EMPTY_SET;
   }

   public ServiceRegistry getServices()
   {
      return this.serviceRegistry;
   }
   
   public String getId()
   {
      return "se-module";
   }

}
