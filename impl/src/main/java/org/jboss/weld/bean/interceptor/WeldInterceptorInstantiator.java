package org.jboss.weld.bean.interceptor;

import org.jboss.interceptor.spi.instance.InterceptorInstantiator;
import org.jboss.interceptor.spi.metadata.ClassMetadata;
import org.jboss.interceptor.spi.metadata.InterceptorReference;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.reflection.SecureReflections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import java.lang.reflect.Constructor;

/**
 * @author Marius Bogoevici
 * @param <T>
 */
public class WeldInterceptorInstantiator<T> implements InterceptorInstantiator<T, Object>
{

   private BeanManagerImpl manager;

   private CreationalContext<T> creationalContext;

   public WeldInterceptorInstantiator(BeanManagerImpl manager, CreationalContext<T> creationalContext)
   {
      this.manager = manager;
      this.creationalContext = creationalContext;
   }

   public T createFor(InterceptorReference<Object> interceptorReference)
   {
      if (interceptorReference.getInterceptor() instanceof ClassMetadata<?>)
      {
         try
         {
            // this is not a managed instance - assume no-argument constructor exists
            Class<T> clazz = (Class<T>) interceptorReference.getClassMetadata().getJavaClass();
            Constructor<T> constructor = ((Constructor<T>) SecureReflections.getDeclaredConstructor(clazz));
            T interceptorInstance = SecureReflections.ensureAccessible(constructor).newInstance();
            // inject
            AnnotatedType<T> type = manager.createAnnotatedType(clazz);
            InjectionTarget<T> target = manager.createInjectionTarget(type);
            target.inject(interceptorInstance, creationalContext);
            return interceptorInstance;
         }
         catch (Exception e)
         {
            throw new DeploymentException(e);
         }
      }
      if (interceptorReference.getInterceptor() instanceof SerializableContextual)
      {
         try
         {
            // this is not a managed instance - assume no-argument constructor exists
            SerializableContextual<Interceptor<T>, T> serializableContextual = (SerializableContextual<Interceptor<T>, T>) interceptorReference.getInterceptor();
            return (T) manager.getReference(serializableContextual.get(), creationalContext, false);
         }
         catch (Exception e)
         {
            throw new DeploymentException(e);
         }
      }
      throw new IllegalStateException();
   }
}
