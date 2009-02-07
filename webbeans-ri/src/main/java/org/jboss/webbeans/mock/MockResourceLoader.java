package org.jboss.webbeans.mock;

import java.io.IOException;
import java.net.URL;

import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoadingException;
import org.jboss.webbeans.util.EnumerationIterable;

public class MockResourceLoader implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      try
      {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
      }
      catch (ClassNotFoundException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
   public URL getResource(String name)
   {
      return Thread.currentThread().getContextClassLoader().getResource(name);
   }
   
   public Iterable<URL> getResources(String name)
   {
      try
      {
         return new EnumerationIterable<URL>(Thread.currentThread().getContextClassLoader().getResources(name));
      }
      catch (IOException e)
      {
         throw new ResourceLoadingException(e);
      }
   }
   
}
