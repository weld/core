package org.jboss.weld.bean.interceptor;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorReference;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

import javax.enterprise.inject.spi.Interceptor;
import java.io.Serializable;

public class SerializableContextualInterceptorReference implements InterceptorReference<SerializableContextual<Interceptor<?>,?>>, Serializable
{

   private static final long serialVersionUID = 8653531535170327439L;
   
   private SerializableContextual<Interceptor<?>, ?> interceptor;
   private ClassMetadata<?> classMetadata;

   public SerializableContextualInterceptorReference(SerializableContextual<Interceptor<?>, ?> interceptor, ClassMetadata<?> classMetadata)
   {
      this.interceptor = interceptor;
      this.classMetadata = classMetadata;
   }

   public SerializableContextual<Interceptor<?>, ?> getInterceptor()
   {
      return interceptor;
   }

   public ClassMetadata<?> getClassMetadata()
   {
      return classMetadata;
   }
}
