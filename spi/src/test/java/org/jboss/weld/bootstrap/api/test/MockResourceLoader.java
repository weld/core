package org.jboss.weld.bootstrap.api.test;

import java.net.URL;
import java.util.Collection;

import org.jboss.weld.resources.spi.ResourceLoader;

public class MockResourceLoader extends MockService implements ResourceLoader
{
   
   public Class<?> classForName(String name)
   {
      return null;
   }
   
   public URL getResource(String name)
   {
      return null;
   }
   
   public Collection<URL> getResources(String name)
   {
      return null;
   }
   
}
