package org.jboss.webbeans.bootstrap;

import java.io.IOException;
import java.net.URL;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.EnumerationIterable;

public class SimpleResourceLoader implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      
      try
      {
         return Class.forName(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
      catch (NoClassDefFoundError e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
   public URL getResource(String name)
   {
      return getClass().getResource(name);
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         return new EnumerationIterable<URL>(getClass().getClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
}
