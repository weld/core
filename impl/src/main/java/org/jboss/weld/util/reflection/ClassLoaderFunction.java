package org.jboss.weld.util.reflection;

import org.jboss.weld.resources.spi.ResourceLoader;

import com.google.common.base.Function;

public class ClassLoaderFunction implements Function<String, Class<?>>
{
   
   private final ResourceLoader resourceLoader;  
   
   public ClassLoaderFunction(ResourceLoader resourceLoader)
   {
      this.resourceLoader = resourceLoader;
   }

   public Class<?> apply(String from)
   {
      return resourceLoader.classForName(from);
   }
   
}