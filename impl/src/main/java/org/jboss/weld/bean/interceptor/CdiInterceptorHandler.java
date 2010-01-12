package org.jboss.weld.bean.interceptor;

import java.io.Serializable;

import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.proxy.AbstractClassInterceptionHandler;
import org.jboss.weld.serialization.spi.helpers.SerializableContextualInstance;

/**
 * @author Marius Bogoevici
 */
public class CdiInterceptorHandler<T> extends AbstractClassInterceptionHandler implements Serializable
{

   private static final long serialVersionUID = -1614068925228599196L;
   
   private final SerializableContextualInstance<Interceptor<T>, T> serializableContextualInstance;

   public CdiInterceptorHandler(SerializableContextualInstance<Interceptor<T>, T> serializableContextualInstance, Class<?> clazz)
   {
      super(clazz);
      this.serializableContextualInstance = serializableContextualInstance;
   }

   @Override
   public Object getInterceptorInstance()
   {
      return serializableContextualInstance.getInstance();
   }
}
