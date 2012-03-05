package org.jboss.weld.bean.interceptor;

import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.interceptor.spi.instance.InterceptorInstantiator;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorReference;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;
import java.lang.reflect.Constructor;

import static org.jboss.weld.util.reflection.Reflections.cast;

/**
 * @param <T>
 * @author Marius Bogoevici
 */
public class WeldInterceptorInstantiator<T> implements InterceptorInstantiator<T, Object> {

    private BeanManagerImpl manager;

    private CreationalContext<T> creationalContext;

    public WeldInterceptorInstantiator(BeanManagerImpl manager, CreationalContext<T> creationalContext) {
        this.manager = manager;
        this.creationalContext = creationalContext;
    }

    public T createFor(InterceptorReference<Object> interceptorReference) {
        if (interceptorReference.getInterceptor() instanceof ClassMetadata<?>) {
            try {
                // this is not a managed instance - assume no-argument constructor exists
                Class<T> clazz = cast(interceptorReference.getClassMetadata().getJavaClass());
                Constructor<T> constructor = SecureReflections.getDeclaredConstructor(clazz);
                T interceptorInstance = SecureReflections.ensureAccessible(constructor).newInstance();
                // inject
                AnnotatedType<T> type = manager.createAnnotatedType(clazz);
                InjectionTarget<T> target = manager.createInjectionTarget(type);
                target.inject(interceptorInstance, creationalContext);
                return interceptorInstance;
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
        if (interceptorReference.getInterceptor() instanceof SerializableContextual) {
            try {
                SerializableContextual<Interceptor<T>, T> serializableContextual = cast(interceptorReference.getInterceptor());
                return Reflections.<T>cast(manager.getReference(serializableContextual.get(), creationalContext, false));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        }
        throw new IllegalStateException();
    }
}
