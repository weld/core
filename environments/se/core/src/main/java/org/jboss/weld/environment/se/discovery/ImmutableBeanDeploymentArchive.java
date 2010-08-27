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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Deployment;

/**
 * An immutable implementation of {@link BeanDeploymentArchive} which must have
 * classes and beans.xml resources added to it via
 * {@link ImmutableBeanDeploymentArchive#ImmutableBeanDeploymentArchive(String, Collection, Collection, List)}
 * or
 * {@link ImmutableBeanDeploymentArchive#ImmutableBeanDeploymentArchive(String, Collection, Collection)}
 * 
 * See {@link Deployment} for more detailed information on creating deployment
 * structures.
 * 
 * @author Pete Muir
 * 
 */
public class ImmutableBeanDeploymentArchive extends AbstractWeldSEBeanDeploymentArchive
{

   private final Collection<String> beanClasses;
   private final BeansXml beansXml;
   private final Collection<BeanDeploymentArchive> beanDeploymentArchives;

   public ImmutableBeanDeploymentArchive(String id, Collection<String> beanClasses, BeansXml beansXml, Collection<BeanDeploymentArchive> beanDeploymentArchives)
   {
      super(id);
      this.beanClasses = beanClasses;
      this.beansXml = beansXml;
      this.beanDeploymentArchives = beanDeploymentArchives;
   }

   public ImmutableBeanDeploymentArchive(String id, Collection<String> beanClasses, BeansXml beansXml)
   {
      this(id, beanClasses, beansXml, new ArrayList<BeanDeploymentArchive>());
   }

   public Collection<String> getBeanClasses()
   {
      return Collections.unmodifiableCollection(beanClasses);
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.unmodifiableCollection(beanDeploymentArchives);
   }

   public BeansXml getBeansXml()
   {
      return beansXml;
   }
}
