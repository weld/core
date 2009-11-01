package org.jboss.weld.util.serviceProvider;

import org.jboss.weld.bootstrap.api.Service;

public interface ServiceLoaderFactory extends Service
{

   public <S> DefaultServiceLoader<S> load(Class<S> service);
   
   public <S> DefaultServiceLoader<S> load(Class<S> service, ClassLoader loader);

}
