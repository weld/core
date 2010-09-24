package org.jboss.weld.bean.interceptor;

import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorReference;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

import javax.enterprise.inject.spi.Interceptor;
import java.io.Serializable;

/**
* Created by IntelliJ IDEA.
* User: marius
* Date: Sep 27, 2010
* Time: 8:12:08 PM
* To change this template use File | Settings | File Templates.
*/
public class SerializableContextualInterceptorReference implements InterceptorReference<SerializableContextual<Interceptor<?>,?>>, Serializable
{

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
