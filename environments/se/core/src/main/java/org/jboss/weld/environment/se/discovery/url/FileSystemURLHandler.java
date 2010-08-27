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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides file-system orientated scanning
 * 
 * @author Pete Muir
 * 
 */
public class FileSystemURLHandler
{

   private static final Logger log = LoggerFactory.getLogger(FileSystemURLHandler.class);

   public void handle(Collection<String> paths, List<String> discoveredClasses, List<URL> discoveredBeansXmlUrls)
   {
      for (String urlPath : paths)
      {
         try
         {
            FileSystemURLHandler.log.trace("scanning: " + urlPath);

            if (urlPath.startsWith("file:"))
            {
               urlPath = urlPath.substring(5);
            }
            if (urlPath.indexOf('!') > 0)
            {
               urlPath = urlPath.substring(0, urlPath.indexOf('!'));
            }

            File file = new File(urlPath);
            if (file.isDirectory())
            {
               handleDirectory(file, null, discoveredClasses, discoveredBeansXmlUrls);
            }
            else
            {
               handleArchiveByFile(file, discoveredClasses, discoveredBeansXmlUrls);
            }
         }
         catch (IOException ioe)
         {
            FileSystemURLHandler.log.warn("could not read entries", ioe);
         }
      }
   }

   private void handleArchiveByFile(File file, List<String> discoveredClasses, List<URL> discoveredBeansXmlUrls) throws IOException
   {
      try
      {
         log.trace("archive: " + file);

         String archiveUrl = "jar:" + file.toURI().toURL().toExternalForm() + "!/";
         ZipFile zip = new ZipFile(file);
         Enumeration<? extends ZipEntry> entries = zip.entries();

         while (entries.hasMoreElements())
         {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            handle(name, new URL(archiveUrl + name), discoveredClasses, discoveredBeansXmlUrls);
         }
      }
      catch (ZipException e)
      {
         throw new RuntimeException("Error handling file " + file, e);
      }
   }

   protected void handleDirectory(File file, String path, List<String> discoveredClasses, List<URL> discoveredBeansXmlUrls)
   {
      handleDirectory(file, path, new File[0], discoveredClasses, discoveredBeansXmlUrls);
   }

   private void handleDirectory(File file, String path, File[] excludedDirectories, List<String> discoveredClasses, List<URL> discoveredBeansXmlUrls)
   {
      for (File excludedDirectory : excludedDirectories)
      {
         if (file.equals(excludedDirectory))
         {
            log.trace("skipping excluded directory: " + file);

            return;
         }
      }

      log.trace("handling directory: " + file);

      for (File child : file.listFiles())
      {
         String newPath = (path == null) ? child.getName() : (path + '/' + child.getName());

         if (child.isDirectory())
         {
            handleDirectory(child, newPath, excludedDirectories, discoveredClasses, discoveredBeansXmlUrls);
         }
         else
         {
            try
            {
               handle(newPath, child.toURI().toURL(), discoveredClasses, discoveredBeansXmlUrls);
            }
            catch (MalformedURLException e)
            {
               log.error("Error loading file " + newPath);
            }
         }
      }
   }

   protected void handle(String name, URL url, List<String> discoveredClasses, List<URL> discoveredBeansXmlUrls)
   {
      if (name.endsWith(".class"))
      {
         discoveredClasses.add(filenameToClassname(name));
      }
      else if (name.endsWith("beans.xml"))
      {
         discoveredBeansXmlUrls.add(url);
      }
   }

   /**
    * Convert a path to a class file to a class name
    */
   public static String filenameToClassname(String filename)
   {
      return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
   }
}
