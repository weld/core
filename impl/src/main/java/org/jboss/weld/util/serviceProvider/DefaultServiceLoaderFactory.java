package org.jboss.weld.util.serviceProvider;

public class DefaultServiceLoaderFactory implements ServiceLoaderFactory
{

   public void cleanup() {}

   public <S> DefaultServiceLoader<S> load(Class<S> service)
   {
      return DefaultServiceLoader.load(service);
   }
   
   public <S> DefaultServiceLoader<S> load(Class<S> service, ClassLoader loader)
   {
      return DefaultServiceLoader.load(service, loader);
   }

}
