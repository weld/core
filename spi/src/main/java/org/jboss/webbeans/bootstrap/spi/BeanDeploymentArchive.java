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

import org.jboss.webbeans.ejb.spi.EJBModule;

/**
 * Represents a CDI bean deployment archive.
 * 
 * A bean deployment archive is any library jar, EJB jar or rar archive with a
 * META-INF/beans.xml file, any WEB-INF/classes directory in war with a
 * WEB-INF/beans.xml, or any directory in the classpath with a
 * META-INF/beans.xml.
 * 
 * For an application deployed as an ear, all library jars, EJB jars, rars and
 * war WEB-INF/classes directories should be searched.
 * 
 * For an application deployed as a war, all library jars and the
 * WEB-INF/classes directory should be searched.
 * 
 * The container is allowed to specify archives as {@link BeanDeploymentArchive}
 * even if no beans.xml is present.
 * 
 * @see EJBModule
 * 
 * @author Pete Muir
 * 
 */
public interface BeanDeploymentArchive
{

   /**
    * Get the ordered transitive closure of modules which are accessible to this
    * module. The order will be used both in bean discovery and resolution.
    * 
    * Circular dependencies will be detected and ignored by the container
    * 
    * @return the ordered transitive closure
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchiveClosure();

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
