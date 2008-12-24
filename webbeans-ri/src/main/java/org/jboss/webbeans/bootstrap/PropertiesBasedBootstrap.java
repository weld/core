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
   
   

   protected abstract DeploymentProperties getDeploymentProperties();
   
}