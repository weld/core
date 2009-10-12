package org.jboss.weld.bean.interceptor;

import java.io.Serializable;

import javax.enterprise.inject.spi.Interceptor;

import org.jboss.interceptor.proxy.AbstractClassInterceptionHandler;
import org.jboss.weld.context.SerializableContextualInstance;

/**
 * @author Marius Bogoevici
 */
public class CdiInterceptorHandler extends AbstractClassInterceptionHandler implements Serializable
{

   private final SerializableContextualInstance<Interceptor<Object>, Object> serializableContextualInstance;

   public CdiInterceptorHandler(SerializableContextualInstance<Interceptor<Object>, Object> serializableContextualInstance, Class<?> clazz)
   {
      super(clazz);
      this.serializableContextualInstance = serializableContextualInstance;
   }

   public Object getInterceptorInstance()
   {
      return serializableContextualInstance.getInstance();
   }
}
