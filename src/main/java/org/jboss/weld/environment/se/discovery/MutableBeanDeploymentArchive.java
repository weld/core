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
import java.util.HashSet;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;

/**
 * A mutable implementation of {@link BeanDeploymentArchive} which can have
 * classes and beans.xml resources added to it by calling
 * <code>getBeanClasses.add()</code> and <code>getBeansXml().add()</code>
 * respectively.
 * 
 * If you are building a complex deployment structure, you can also associate
 * accessible {@link BeanDeploymentArchive}s by calling
 * <code>getBeanDeploymentArchives().add()</code>. See {@link Deployment} for
 * more detailed information on creating deployment structures.
 * 
 * @author Pete Muir
 * 
 */
public class MutableBeanDeploymentArchive extends AbstractWeldSEBeanDeploymentArchive
{

   private final Collection<Class<?>> beanClasses;
   private final Collection<URL> beansXml;
   private final List<BeanDeploymentArchive> beanDeploymentArchives;

   public MutableBeanDeploymentArchive(String id)
   {
      super(id);
      this.beanClasses = new HashSet<Class<?>>();
      this.beansXml = new HashSet<URL>();
      this.beanDeploymentArchives = new ArrayList<BeanDeploymentArchive>();
   }

   public Collection<Class<?>> getBeanClasses()
   {
      return beanClasses;
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return beanDeploymentArchives;
   }

   public Collection<URL> getBeansXml()
   {
      return beansXml;
   }
}
