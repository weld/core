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

import javax.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.environment.servlet.deployment.ServletDeployment;
import org.jboss.weld.environment.servlet.deployment.URLScanner;
import org.jboss.weld.environment.servlet.deployment.WebAppBeanDeploymentArchive;

/**
 * Exact listener.
 *
 * Explicitly list all the bean classes in beans.txt file.
 * This will reduce scanning; e.g. useful for restricted env like GAE
 *
 * @author Ales Justin
 */
public class ExactListener extends Listener
{
   protected ServletDeployment createServletDeployment(ServletContext context, Bootstrap bootstrap)
   {
      return new ExactServletDeployment(context, bootstrap);
   }

   private static class ExactServletDeployment extends ServletDeployment
   {
      private ExactServletDeployment(ServletContext servletContext, Bootstrap bootstrap)
      {
         super(servletContext, bootstrap);
      }

      protected WebAppBeanDeploymentArchive createWebAppBeanDeploymentArchive(ServletContext servletContext, Bootstrap bootstrap)
      {
         return new ExactWebAppBeanDeploymentArchive(servletContext, bootstrap);
      }
   }

   private static class ExactWebAppBeanDeploymentArchive extends WebAppBeanDeploymentArchive
   {
      private ExactWebAppBeanDeploymentArchive(ServletContext servletContext, Bootstrap bootstrap)
      {
         super(servletContext, bootstrap);
      }

      protected URLScanner createScanner(ClassLoader classLoader)
      {
         return new ExactScanner(classLoader);
      }
   }

   private static class ExactScanner extends URLScanner
   {
      private ExactScanner(ClassLoader classLoader)
      {
         super(classLoader);
      }

      public void scanResources(String[] resources, List<String> classes, List<URL> urls)
      {
         URL url = getClassLoader().getResource("beans.txt");
         if (url == null)
            throw new IllegalArgumentException("Missing beans.txt");

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
