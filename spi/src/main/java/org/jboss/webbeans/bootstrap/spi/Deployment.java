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

import java.util.List;

import org.jboss.webbeans.bootstrap.api.Service;

/**
 * Represents a deployment of a CDI application.
 * 
 * Web Beans will request the bean archive deployment structure during the bean
 * discovery initialization step. After this step, CDI allows users to define
 * bean's programmatically, possibly with bean classes from a deployment archive
 * which is currently not a bean deployment archive. Web Beans will request the
 * {@link BeanDeploymentArchive} for each programmatically using
 * {@link #loadBeanDeploymentArchive(Class)}. If any unknown
 * {@link BeanDeploymentArchive}s are loaded, before Web Beans proceeds to
 * validating the deployment, the bean archive deployment structure will
 * re-requested.
 * 
 * For an application deployed as an ear to a Java EE container, all library
 * jars, EJB jars, rars and war WEB-INF/classes directories should be searched,
 * and the bean deployment archive structure built.
 * 
 * For an application deployed as a war to a Java EE or Servlet container, all
 * library jars and the WEB-INF/classes directory should be searched, and the
 * bean deployment archive structure built.
 * 
 * TODO Java SE structure
 * For an application deployed in the SE environment, all library jars and
 * classpath directories should be searched, and the bean deployment archive
 * structure built. A single, logical deployment archive will be built for
 * all beans and beans.xml files found on the classpath.
 * 
 * @see BeanDeploymentArchive
 * 
 * @author Pete Muir
 * 
 */
public interface Deployment extends Service
{

   /**
    * Get the bean deployment archives which are accessible by this deployment
    * and adjacent to it in the deployment archive graph.
    * 
    * The bean deployment archives will be processed in the order specified.
    * 
    * Circular dependencies will be detected and ignored by the container
    * 
    * @return the ordered accessible bean deployment archives
    * 
    */
   public List<BeanDeploymentArchive> getBeanDeploymentArchives();

   /**
    * Load the {@link BeanDeploymentArchive} containing the given class.
    * 
    * If the deployment archive containing the given class is not currently a
    * bean deployment archive, it should be added to the bean deployment archive
    * graph and returned. If the deployment archive is currently a bean
    * deployment archive it should be returned.
    * 
    * @param beanClass the bean class to load
    * @return the {@link BeanDeploymentArchive} containing the bean class
    */
   public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass);

}
