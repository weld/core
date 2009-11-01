package org.jboss.weld.util.serviceProvider;

import java.util.Arrays;
import java.util.Collection;


public class PackageServiceLoaderFactory extends DefaultServiceLoaderFactory
{
   
   private final String directoryName;
   private final Collection<Class<?>> classes;
   
   public PackageServiceLoaderFactory(Package pkg, Class<?>... classes)
   {
      this.directoryName = pkg.getName().replace(".", "/");
      this.classes = Arrays.asList(classes);
   }
   
   @Override
   public <S> DefaultServiceLoader<S> load(Class<S> service)
   {
      if (classes.contains(service))
      {
         return DefaultServiceLoader.load(directoryName, service);
      }
      else
      {
         return super.load(service);
      }
   }
   
   @Override
   public <S> DefaultServiceLoader<S> load(Class<S> service, ClassLoader loader)
   {
      if (classes.contains(service))
      {
         return DefaultServiceLoader.load(directoryName, service, loader);
      }
      else
      {
         return super.load(service, loader);
      }
   }

}
