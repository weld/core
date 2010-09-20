/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.impl.base.Validate;
import org.jboss.shrinkwrap.impl.base.asset.ArchiveAsset;

/**
 * BeanUtils
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
final class Utils
{
   private Utils() {}
   
   @SuppressWarnings("unchecked")
   static <T> T getBeanReference(BeanManager manager, Class<T> type) 
   {
      Bean<?> bean = manager.resolve(manager.getBeans(type));
      return (T)manager.getReference(
            bean, 
            type,
            manager.createCreationalContext(null));
   }

   public static String findArchiveId(Archive<?> archive)
   {
      return archive.getName();
   }
   
   public static Collection<URL> findBeansXml(Archive<?> archive)
   {
      Validate.notNull(archive, "Archive must be specified");
      List<URL> beansXmls = new ArrayList<URL>();
      Map<ArchivePath, Node> nestedArchives = archive.getContent(Filters.include(".*\\.jar|.*\\.war"));
      for(final Map.Entry<ArchivePath, Node> nestedArchiveEntry : nestedArchives.entrySet())
      {
         if( !(nestedArchiveEntry.getValue().getAsset() instanceof ArchiveAsset))
         {
            continue;
         }
         ArchiveAsset nestedArchive = (ArchiveAsset)nestedArchiveEntry.getValue().getAsset();
         Map<ArchivePath, Node> classes = nestedArchive.getArchive().getContent(Filters.include(".*/beans.xml"));
         for(final Map.Entry<ArchivePath, Node> entry : classes.entrySet()) 
         {
            try 
            {
               beansXmls.add(
                     new URL(null, "archive://" + entry.getKey().get(), new URLStreamHandler() 
                     {
                        @Override
                        protected java.net.URLConnection openConnection(URL u) throws java.io.IOException 
                        {
                           return new URLConnection(u)
                           {
                              @Override
                              public void connect() throws IOException { }
                              
                              @Override
                              public InputStream getInputStream()
                                    throws IOException
                              {
                                 return entry.getValue().getAsset().openStream();
                              }
                           };
                        };
                     }));
            } 
            catch (Exception e) {
               e.printStackTrace();
            }
         }
      }
      Map<ArchivePath, Node> classes = archive.getContent(Filters.include(".*/beans.xml"));
      for(final Map.Entry<ArchivePath, Node> entry : classes.entrySet()) 
      {
         try 
         {
            beansXmls.add(
                  new URL(null, "archive://" + entry.getKey().get(), new URLStreamHandler() 
                  {
                     @Override
                     protected java.net.URLConnection openConnection(URL u) throws java.io.IOException 
                     {
                        return new URLConnection(u)
                        {
                           @Override
                           public void connect() throws IOException { }
                           
                           @Override
                           public InputStream getInputStream()
                                 throws IOException
                           {
                              return entry.getValue().getAsset().openStream();
                           }
                        };
                     };
                  }));
         } 
         catch (Exception e) {
            e.printStackTrace();
         }
      }
      return beansXmls;
   }

   public static Collection<Class<?>> findBeanClasses(Archive<?> archive, ClassLoader classLoader)
   {
      Validate.notNull(archive, "Archive must be specified");
      List<Class<?>> beanClasses = new ArrayList<Class<?>>();

      try
      {
         Map<ArchivePath, Node> nestedArchives = archive.getContent(Filters.include(".*\\.jar|.*\\.war|.*\\.rar"));
         for(final Map.Entry<ArchivePath, Node> nestedArchiveEntry : nestedArchives.entrySet())
         {
            if( !(nestedArchiveEntry.getValue().getAsset() instanceof ArchiveAsset))
            {
               continue;
            }
            ArchiveAsset nestedArchive = (ArchiveAsset)nestedArchiveEntry.getValue().getAsset();
            Map<ArchivePath, Node> classes = nestedArchive.getArchive().getContent(Filters.include(".*\\.class"));
            for(Map.Entry<ArchivePath, Node> classEntry : classes.entrySet()) 
            {
               Class<?> loadedClass = classLoader.loadClass(
                     findClassName(classEntry.getKey())); 
   
               beanClasses.add(loadedClass);
            }
         }
         Map<ArchivePath, Node> classes = archive.getContent(Filters.include(".*\\.class"));
         for(Map.Entry<ArchivePath, Node> classEntry : classes.entrySet()) 
         {
            Class<?> loadedClass = classLoader.loadClass(
                  findClassName(classEntry.getKey())); 
   
            beanClasses.add(loadedClass);
         }
      }
      catch (ClassNotFoundException e) 
      {
         throw new RuntimeException("Could not load class from archive " + archive.getName(), e);
      }
      return beanClasses;
   }
   
   /*
    *  input:  /org/MyClass.class
    *  output: org.MyClass
    */
   public static String findClassName(ArchivePath path)
   {
      String className = path.get();
      className = className.replaceAll("/WEB-INF/classes/", "");
      if(className.charAt(0) == '/')
      {
         className = className.substring(1);
      }
      className = className.replaceAll("\\.class", "");
      className = className.replaceAll("/", ".");
      return className;
   }

}
