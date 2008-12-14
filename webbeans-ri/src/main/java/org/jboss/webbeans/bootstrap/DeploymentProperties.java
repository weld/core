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

package org.jboss.webbeans.bootstrap;

import static org.jboss.webbeans.util.Strings.split;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * Utility class to load deployment properties
 * 
 * @author Pete Muir
 */
public class DeploymentProperties
{
   // The resource bundle used to control Web Beans RI deployment
   public static final String RESOURCE_BUNDLE = "META-INF/web-beans-ri.properties";

   // The class to work from
   private ClassLoader classLoader;
   // An enumeration of URLs to work on
   private Enumeration<URL> urlEnum;

   /**
    * Constructor
    * 
    * @param classLoader The classloader to work on
    */
   public DeploymentProperties(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   /**
    * Get a list of possible values for a given key.
    * 
    * First, System properties are tried, followed by the specified resource
    * bundle (first in classpath only).
    * 
    * Colon (:) deliminated lists are split out. (gotta love Petes choice of
    * ASCII art for that one ;-)
    * 
    * @param key The key to search for
    * @return A list of possible values. An empty list is returned if there are
    *         no matches.
    */
   public List<String> getPropertyValues(String key)
   {
      List<String> values = new ArrayList<String>();
      addPropertiesFromSystem(key, values);
      addPropertiesFromResourceBundle(key, values);
      return values;
   }

   /**
    * Adds matches from system properties
    * 
    * @param key The key to match
    * @param values The currently found values
    */
   private void addPropertiesFromSystem(String key, List<String> values)
   {
      addProperty(key, System.getProperty(key), values);
   }

   /**
    * Adds matches from detected resource bundles
    * 
    * @param key The key to match
    * @param values The currently found values
    */
   private void addPropertiesFromResourceBundle(String key, List<String> values)
   {
      try
      {
         while (getResources().hasMoreElements())
         {
            URL url = getResources().nextElement();
            Properties properties = new Properties();
            InputStream propertyStream = url.openStream();
            try
            {
               properties.load(propertyStream);
               addProperty(key, properties.getProperty(key), values);
            }
            finally
            {
               if (propertyStream != null)
               {
                  propertyStream.close();
               }
            }
         }
      }
      catch (IOException e)
      {
         // No - op, file is optional
      }
   }

   /**
    * Add the property to the set of properties only if it hasn't already been
    * added
    * 
    * @param key The key searched for
    * @param value The value of the property
    * @param values The currently found values
    */
   private void addProperty(String key, String value, List<String> values)
   {
      if (value != null)
      {
         String[] properties = split(value, ":");
         for (String property : properties)
         {
            values.add(property);
         }

      }
   }

   /**
    * Gets all Web Beans property files relative to the provided classloader
    * 
    * @return An enumeration of URLs to the property files
    * @throws IOException If the resource files could not be loaded
    */
   private Enumeration<URL> getResources() throws IOException
   {

      if (urlEnum == null)
      {
         urlEnum = classLoader.getResources(RESOURCE_BUNDLE);
      }
      return urlEnum;
   }

}
