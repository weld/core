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
package org.jboss.weld.environment.servlet;

import org.jboss.weld.environment.servlet.deployment.URLScanner;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Exact listener.
 *
 * Explicitly list all the bean classes in bean-classes.txt file.
 * This will reduce scanning; e.g. useful for restricted env like GAE
 *
 * @author Ales Justin
 */
public class ExactListener extends Listener
{
   public static final String BEAN_CLASSES = "bean-classes.txt";

   protected URLScanner createUrlScanner(ClassLoader classLoader, ServletContext context)
   {
      return new ExactScanner(classLoader, context);
   }

   private static class ExactScanner extends URLScanner
   {
      private ServletContext context;

      private ExactScanner(ClassLoader classLoader, ServletContext context)
      {
         super(classLoader);

         if (context == null)
            throw new IllegalArgumentException("Null context");

         this.context = context;
      }

      protected URL getExactBeansURL()
      {
         try
         {
            URL url = context.getResource("/WEB-INF/" + BEAN_CLASSES);
            if (url == null)
               url = getClassLoader().getResource(BEAN_CLASSES);

            return url;
         }
         catch (MalformedURLException e)
         {
            throw new RuntimeException(e);
         }
      }

      @Override
      public void scanResources(String[] resources, Set<String> classes, Set<URL> urls)
      {
         URL url = getExactBeansURL();
         if (url == null)
            throw new IllegalArgumentException("Missing exact beans resource: " + BEAN_CLASSES);

         try
         {
            InputStream is = url.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            try
            {
               String line;
               while((line = reader.readLine()) != null)
               {
                  classes.add(line);
               }
            }
            finally
            {
               try
               {
                  is.close();
               }
               catch (IOException ignore)
               {
               }
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
