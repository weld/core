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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.webbeans.ExecutionException;

import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.DefaultNaming;
import org.jboss.webbeans.resources.spi.Naming;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.ServletBootstrap;
import org.jboss.webbeans.util.DeploymentProperties;
import org.jboss.webbeans.util.Reflections;

/**
 * An abstract extension of Bootstrap which uses deployment properties for 
 * configuring the application
 * 
 * @author Pete Muir
 *
 */
public abstract class PropertiesBasedBootstrap extends WebBeansBootstrap
{
   // The log provider
   private static final LogProvider log = Logging.getLogProvider(ServletBootstrap.class);
   
   /**
    * Returns any class constructor from the merged list defined by the 
    * specified property.
    * No guarantee is made about which item in the list will returned.
    * 
    * @param <T> The class type
    * @param deploymentProperties The deployment properties to be used
    * @param resourceLoader The resourceLoader to use for class and resource loading
    * @param propertyName The name of the property to find in the deployment properties
    * @param expectedType The expected type or super type of the class
    * @param constructorArguments The arguments of the constructor to select
    * @return
    */
   protected static <T> Constructor<? extends T> getClassConstructor(DeploymentProperties deploymentProperties, ResourceLoader resourceLoader, String propertyName, Class<T> expectedType, Class<?> ... constructorArguments)
   {
      for (Class<? extends T> clazz : DeploymentProperties.getClasses(deploymentProperties, resourceLoader, propertyName, expectedType))
      {
         Constructor<? extends T> constructor = Reflections.getConstructor((Class<? extends T>) clazz, constructorArguments);
         if (constructor != null)
         {
            return constructor;
         }
      }
      return null;
   }
   
   /**
    * Creates an instance of the type
    * 
    * @param constructor The constructor to use
    * @param parameters The parameters to pass to the contstructor
    * @return An instance of the type
    */
   protected static <T> T newInstance(Constructor<T> constructor, Object... parameters)
   {
      try
      {
         return constructor.newInstance(parameters);
      }
      catch (IllegalArgumentException e)
      {
         throw new ExecutionException("Error instantiating " + constructor, e);
      }
      catch (InstantiationException e)
      {
         throw new ExecutionException("Error instantiating " + constructor, e);
      }
      catch (IllegalAccessException e)
      {
         throw new ExecutionException("Error instantiating " + constructor, e);
      }
      catch (InvocationTargetException e)
      {
         throw new ExecutionException("Error instantiating " + constructor, e);
      }
   }
   
   /**
    * Initializes the naming provider
    * 
    * Only safe to call once resourceloader and deployment properties are set
    */
   protected void initProperties()
   {
      Constructor<? extends Naming> namingConstructor = getClassConstructor(getDeploymentProperties(), getResourceLoader(), Naming.PROPERTY_NAME, Naming.class);
      if (namingConstructor != null)
      {
         getManager().setNaming(newInstance(namingConstructor));
      }
      else
      {
         getManager().setNaming(new DefaultNaming());
      }
   }
   
   
   /**
    * Gets the deployment properties
    * 
    * @return The deployment properties
    * 
    * @see org.jboss.webbeans.util.DeploymentProperties
    */
   protected abstract DeploymentProperties getDeploymentProperties();
   
}