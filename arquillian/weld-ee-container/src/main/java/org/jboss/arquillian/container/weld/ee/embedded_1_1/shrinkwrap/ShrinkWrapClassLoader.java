/*
 * JBoss, Community-driven Open Source Middleware
 * Copyright 2010, JBoss by Red Hat, Inc., and individual contributors
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
package org.jboss.arquillian.container.weld.ee.embedded_1_1.shrinkwrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

/**
 * A ClassLoader implementation that can locate resources in a ShrinkWrap archive
 *
 * <p><strong>NOTE</strong> This is a prototype implementation of this concept. It 
 * does not address all classloading concerns as of yet.</p>
 *
 * @author <a href="mailto:dan.allen@mojavelinux.com">Dan Allen</a>
 */
public class ShrinkWrapClassLoader extends SecureClassLoader
{
   public static final String ARCHIVE_PROTOCOL = "archive:/";

   private final Archive<?> archive;

   public ShrinkWrapClassLoader(Archive<?> archive)
   {
      super();
      this.archive = archive;
   }

   @Override
   protected URL findResource(final String name)
   {
      final Node a = archive.get(name);
      if (a == null)
      {
         return null;
      }
      try
      {
         return new URL(null, ARCHIVE_PROTOCOL + name, new URLStreamHandler()
         {
            @Override
            protected java.net.URLConnection openConnection(URL u) throws java.io.IOException
            {
               return new URLConnection(u)
               {
                  @Override
                  public void connect() throws IOException
                  {
                  }

                  @Override
                  public InputStream getInputStream()
                        throws IOException
                  {
                     return a.getAsset().openStream();
                  }
               };
            }

            ;
         });
      }
      catch (Exception e)
      {
         return null;
      }
   }

   @Override
   protected Enumeration<URL> findResources(String name) throws IOException
   {
      Iterator<URL> it = new ArrayList<URL>(0).iterator();
      URL resource = findResource(name);
      if (resource != null)
      {
         it = Arrays.asList(resource).iterator();
      }
      final Iterator<URL> i = it;
      return new Enumeration<URL>()
      {
         public boolean hasMoreElements()
         {
            return i.hasNext();
         }

         public URL nextElement()
         {
            return i.next();
         }
      };
   }
}
