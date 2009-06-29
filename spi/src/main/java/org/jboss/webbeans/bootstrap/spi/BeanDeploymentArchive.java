/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.webbeans.bootstrap.spi;

import java.net.URL;
import java.util.List;

import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.ejb.spi.EJBModule;

/**
 * Represents a CDI bean deployment archive.
 * 
 * A deployment archive is any library jar, library directory, EJB jar, rar
 * archive or any war WEB-INF/classes directory contained in the Java EE
 * deployment (as defined in the Java Platform, Enterprise Edition (Java EE)
 * Specification, v6, Section 8.1.2).
 * 
 * TODO Java SE definition of a deployment archive
 * 
 * A bean deployment archive is any deployment archive with a META-INF/beans.xml
 * file, or for a war, with a WEB-INF/beans.xml.
 * 
 * The container is allowed to specify a deployment archive as
 * {@link BeanDeploymentArchive} even if no beans.xml is present (for example, a
 * container could define a deployment archive with container specific metadata
 * to be a bean deployment archive).
 * 
 * @see EJBModule
 * @see Deployment
 * 
 * @author Pete Muir
 * 
 */
public interface BeanDeploymentArchive extends Service
{

   /**
    * Get the ordered bean deployment archives which are accessible to this bean
    * deployment archive and adjacent to it in the deployment archive graph.
    * 
    * The bean deployment archives will be processed in the order specified.
    * 
    * Circular dependencies will be detected and ignored by the container
    * 
    * @return the ordered accessible bean deployment archives
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchives();

   /**
    * Gets all classes in the bean deployment archive
    * 
    * @return an iteration over the classes, empty if no classes are present
    */
   public Iterable<Class<?>> getBeanClasses();

   /**
    * Get the deployment descriptor
    * 
    * @return a URL pointing to the deployment descriptor, or null if it is not
    *         present
    */
   public URL getBeansXml();

}
