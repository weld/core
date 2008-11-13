package org.jboss.webbeans.bootstrap;

import static org.jboss.webbeans.util.Strings.split;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/*
 * Utility class to load deployment properties
 */
public class DeploymentProperties
{
   
   private ClassLoader classLoader;
   private Enumeration<URL> urlEnum;
   
   public DeploymentProperties(ClassLoader classLoader)
   {
      this.classLoader = classLoader;
   }

   /**
    * The resource bundle used to control Web Beans RI deployment
    */
   public static final String RESOURCE_BUNDLE = "META-INF/web-beans-ri.properties";
   
   /**
    * Get a list of possible values for a given key.
    * 
    * First, System properties are tried, followed by the specified resource
    * bundle (first in classpath only).
    * 
    * Colon (:) deliminated lists are split out.
    * 
    */
   public List<String> getPropertyValues(String key)
   {
      List<String>values = new ArrayList<String>();
      addPropertiesFromSystem(key, values);
      addPropertiesFromResourceBundle(key, values);
      return values;
   }
   
   private void addPropertiesFromSystem(String key, List<String> values)
   {
      addProperty(key, System.getProperty(key), values);
   }
   
   private void addPropertiesFromResourceBundle(String key, List<String> values)
   {
      try
      {  
         while ( getResources().hasMoreElements() )
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
   
   /*
    * Add the property to the set of properties only if it hasn't already been added
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
   
   private Enumeration<URL> getResources() throws IOException
   {
      
      if (urlEnum == null)
      {
         urlEnum = classLoader.getResources(RESOURCE_BUNDLE);
      }
      return urlEnum;
   }
   
}
