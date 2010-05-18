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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.jboss.weld.environment.se.discovery.handlers.URLHandler;
import org.jboss.weld.environment.se.discovery.handlers.FileSystemURLHandler;
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
public class URLScanner extends AbstractScanner
{

   private static final String FILE = "file";
   private static final String JAR = "jar";
   private final Map<String, URLHandler> urlHandlers = new HashMap<String, URLHandler>();
   private static final Logger log = LoggerFactory.getLogger(URLScanner.class);

   public URLScanner(ResourceLoader resourceLoader, SEWeldDiscovery weldDiscovery)
   {
      super(resourceLoader, weldDiscovery);
      URLHandler fileSysHandler = new FileSystemURLHandler(resourceLoader, weldDiscovery);
      urlHandlers.put(FILE, fileSysHandler);
      urlHandlers.put(JAR, fileSysHandler);
   }

   public void setURLHandler(String type, URLHandler handler)
   {
      urlHandlers.put(type, handler);
   }

   public void scanDirectories(File[] directories)
   {
      for (File directory : directories)
      {
         // can only use a file-based scanner to scan directories
         urlHandlers.get(FILE).handleDirectory(directory);
      }
   }

   public void scanResources(String[] resources)
   {
      Multimap<String, String> paths = HashMultimap.create();
      for (String resourceName : resources)
      {
         // grab all the URLs for this resource
         Collection<URL> urlEnum = getResourceLoader().getResources(resourceName);
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
               } else
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
            } catch (UnsupportedEncodingException ex)
            {
               throw new ClasspathScanningException("Error decoding URL using UTF-8");
            }

            log.debug("URL Type: " + urlType);

            paths.put(urlType, urlPath);

         }
      }
      for (String urlType : paths.keySet())
      {
         Collection<String> urlPaths = paths.get(urlType);
         URLHandler handler = urlHandlers.get(urlType);
         if (handler == null)
         {
            throw new ClasspathScanningException("No handler defined for URL type: " + urlType);
         } else
         {
            handler.handle(urlPaths);
         }
      }
   }
}
