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
package org.jboss.weld.environment.se.discovery.url;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.environment.se.discovery.Scanner;
import org.jboss.weld.environment.se.discovery.WeldSEBeanDeploymentArchive;
import org.jboss.weld.environment.se.exceptions.ClasspathScanningException;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Scanner} which can scan a {@link URLClassLoader}
 * 
 * @author Thomas Heute
 * @author Gavin King
 * @author Norman Richards
 * @author Pete Muir
 * @author Peter Royle
 * 
 */
public class URLScanner implements Scanner
{

   private static final Logger log = LoggerFactory.getLogger(URLScanner.class);
   private final WeldSEBeanDeploymentArchive beanDeploymentArchive;
   private final String[] resources;

   public URLScanner(String... resources)
   {
      this.beanDeploymentArchive = new WeldSEBeanDeploymentArchive("weld-se");
      this.resources = resources;
   }

   public void scan(ResourceLoader resourceLoader)
   {
      FileSystemURLHandler handler = new FileSystemURLHandler(resourceLoader);
      Collection<String> paths = new ArrayList<String>();
      for (String resourceName : resources)
      {
         // grab all the URLs for this resource
         Collection<URL> urlEnum = resourceLoader.getResources(resourceName);
         for (URL url : urlEnum)
         {

            String urlPath = url.toExternalForm();

            // determin resource type (eg: jar, file, bundle)
            String urlType = "file";
            int colonIndex = urlPath.indexOf(":");
            if (colonIndex != -1)
            {
               urlType = urlPath.substring(0, colonIndex);
            }

            // Extra built-in support for simple file-based resources
            if ("file".equals(urlType) || "jar".equals(urlType))
            {
               // switch to using getPath() instead of toExternalForm()
               urlPath = url.getPath();

               if (urlPath.indexOf('!') > 0)
               {
                  urlPath = urlPath.substring(0, urlPath.indexOf('!'));
               }
               else
               {
                  // hack for /META-INF/beans.xml
                  File dirOrArchive = new File(urlPath);
                  if ((resourceName != null) && (resourceName.lastIndexOf('/') > 0))
                  {
                     dirOrArchive = dirOrArchive.getParentFile();
                  }
                  urlPath = dirOrArchive.getParent();
               }
            }

            try
            {
               urlPath = URLDecoder.decode(urlPath, "UTF-8");
            }
            catch (UnsupportedEncodingException ex)
            {
               throw new ClasspathScanningException("Error decoding URL using UTF-8");
            }

            log.debug("URL Type: " + urlType);

            paths.add(urlPath);
         }
         handler.handle(paths, beanDeploymentArchive);
      }
   }
   
   public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> clazz)
   {
      return beanDeploymentArchive;
   }
   
   public List<BeanDeploymentArchive> getBeanDeploymentArchives()
   {
      return Collections.<BeanDeploymentArchive>singletonList(beanDeploymentArchive);
   }
}
