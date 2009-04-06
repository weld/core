package org.jboss.webbeans.bootstrap.api.test;

import java.net.URL;

import org.jboss.webbeans.resources.spi.ResourceLoader;

public class MockResourceLoader implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      return null;
   }
   
   public URL getResource(String name)
   {
      return null;
   }
   
   public Iterable<URL> getResources(String name)
   {
      return null;
   }
   
}
