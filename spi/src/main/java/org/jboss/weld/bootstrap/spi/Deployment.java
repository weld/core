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
package org.jboss.weld.bootstrap.spi;

import java.util.Collection;

import org.jboss.weld.bootstrap.api.ServiceRegistry;

/**
 * Represents a deployment of a CDI application.
 * 
 * <h3>Background</h3>
 * 
 * <p>
 * Java EE defines a series of accessibility rules for classloading, which CDI
 * extends to cover bean resolution. For example a bean in the war can inject a
 * bean in an ejb-jar, but not vice-versa.
 * </p>
 * 
 * <p>
 * These rules are defined via the MANIFEST.MF <a
 * href="http://java.sun.com/javase/6/docs/technotes/guides/extensions/"
 * >Extension Mechanism Architecture</a> and <a
 * href="http://java.sun.com/javase/6/docs/technotes/guides/jar/jar.html">JAR
 * File Specification</a>.
 * </p>
 * 
 * <h3>Deployment structure representation</h3>
 * 
 * <p>
 * Web Beans will request the bean archive deployment structure during the bean
 * discovery initialization step. After this step, CDI allows users to define
 * bean's programmatically, possibly with bean classes from a deployment archive
 * which is currently not a bean deployment archive. Web Beans will request the
 * {@link BeanDeploymentArchive} for each programmatically using
 * {@link #loadBeanDeploymentArchive(Class)}. If any unknown
 * {@link BeanDeploymentArchive}s are loaded, before Web Beans proceeds to
 * validating the deployment, the bean archive deployment structure will
 * re-requested.
 * </p>
 * 
 * <p>
 * The BeanDeploymentArchive tree represents the "logical structure" of the
 * deployment; Given two archives A and B, which have a transitive closure of
 * accessible archives A' and B', it allowable for the the archives A & B to be
 * represented as a single BeanDeploymentArchive, assuming that A' and B' are
 * bi-directionally accessible.
 * </p>
 * 
 * <p>
 * For an application deployed as an ear to a Java EE container, all library
 * jars, EJB jars, rars and war WEB-INF/classes directories should be searched,
 * and the bean deployment archive structure built.
 * </p>
 * 
 * <p>
 * For an application deployed as a war to a Java EE or Servlet container, all
 * library jars and the WEB-INF/classes directory should be searched, and the
 * bean deployment archive structure built.
 * </p>
 * 
 * <p>
 * For an application deployed in the SE environment, all library jars and
 * classpath directories should be searched, and the bean deployment archive
 * structure built. A single, logical deployment archive will be built for all
 * beans and beans.xml files found on the classpath.
 * </p>
 * 
 * <h3>A Java EE example</h3>
 * 
 * <p>
 * Given an ejb-module A (not as part of an ear), an ear B (containing an
 * ejb-jar C which uses the standard mechanism to reference module A, libraries
 * F bundled in /lib, and a war D which references module C), and a war E (which
 * references module A and bundles libraries G in /WEB-INF/lib) all deployed.
 * </p>
 * 
 * <p>
 * War E would have be able to resolve beans from ejb module A, libraries G, as
 * well as other app server libraries. Furthermore, any libraries in G will be
 * able to resolve beans in other libraries G as well as in ejb module A and war
 * E. Ejb module A will not be able to resolve beans in war E or libraries G. It
 * would be legal to represent any libraries G as a single logical
 * BeanDeploymentArchive, also incorportating classes in war E.
 * </p>
 * 
 * <p>
 * War D would be able to resolve beans from from ejb module C, module A
 * (transitively via C), libraries F, as well as other app server libraries. Ejb
 * module C can resolve beans in libraries F as well as module A, but not beans
 * in War D. Ejb module A cannot resolve beans in libraries F, module A or war
 * D. It would be legal to represent libraries F as a single logical
 * BeanDeploymentArchive.
 * </p>
 * 
 * @see BeanDeploymentArchive
 * 
 * @author Pete Muir
 * 
 */
public interface Deployment
{

   /**
    * Get the bean deployment archives which are part of this deployment
    * and adjacent to it in the deployment archive graph. This should include
    * all Java EE modules such as WARs, EJB jars and RARs.
    * 
    * Cycles in the accessible BeanDeploymentArchive graph are allowed. If a 
    * cycle is detected by Web Beans, it will be automatically removed by Web
    * Beans. This means any implementor of this interface don't need to worry
    * about circularities.
    * 
    * @return the accessible bean deployment archives
    * 
    */
   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives();

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
  

   /**
    * Get the services available to this deployment
    * 
    * @return the services available
    */
   public ServiceRegistry getServices();

}
