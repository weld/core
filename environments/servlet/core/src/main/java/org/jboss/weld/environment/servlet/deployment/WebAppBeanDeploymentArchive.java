/**
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
package org.jboss.weld.environment.servlet.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.environment.servlet.util.Reflections;
import org.jboss.weld.environment.servlet.util.Servlets;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 * The means by which Web Beans are discovered on the classpath. This will only
 * discover simple web beans - there is no EJB/Servlet/JPA integration.
 * 
 * @author Peter Royle
 * @author Pete Muir
 * @author Ales Justin
 */
public class WebAppBeanDeploymentArchive implements BeanDeploymentArchive
{
   public static final String META_INF_BEANS_XML = "META-INF/beans.xml";
   public static final String WEB_INF_BEANS_XML = "/WEB-INF/beans.xml";
   public static final String WEB_INF_CLASSES = "/WEB-INF/classes";
   
   private final List<String> classes;
   private final BeansXml beansXml;
   private final ServiceRegistry services;
   
   public WebAppBeanDeploymentArchive(ServletContext servletContext, Bootstrap bootstrap)
   {
      this.services = new SimpleServiceRegistry();
      this.classes = new ArrayList<String>();
      List<URL> urls = new ArrayList<URL>();
      URLScanner scanner = new URLScanner(Reflections.getClassLoader());
      scanner.scanResources(new String[] { META_INF_BEANS_XML }, classes, urls);
      try
      {
         URL beans = servletContext.getResource(WEB_INF_BEANS_XML);
         if (beans != null)
         {
       	   urls.add(beans); // this is consistent with how the JBoss weld.deployer works
            File webInfClasses = Servlets.getRealFile(servletContext, WEB_INF_CLASSES);
            if (webInfClasses != null)
            {
               File[] files = { webInfClasses };
               scanner.scanDirectories(files, classes, urls);
            }
         }
      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException("Error loading resources from servlet context ", e);
      }
      this.beansXml = bootstrap.parse(urls);
   }

   public Collection<String> getBeanClasses()
   {
      return classes;
   }

   public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.emptySet();
   }

   public BeansXml getBeansXml()
   {
      return beansXml;
   }

   public Collection<EjbDescriptor<?>> getEjbs()
   {
      return Collections.emptySet();
   }

   public ServiceRegistry getServices()
   {
      return services;
   }
   
   public String getId()
   {
      // Use "flat" to allow us to continue to use ManagerObjectFactory
      return "flat";
   }
   
}
