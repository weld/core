package org.jboss.weld.interceptor.reader.cache;

import java.util.concurrent.ConcurrentMap;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorFactory;
import org.jboss.weld.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.weld.interceptor.reader.ReflectiveClassMetadata;
import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 *
 */
public class DefaultMetadataCachingReader implements MetadataCachingReader {
    private final ConcurrentMap<InterceptorFactory<?>, InterceptorMetadata<?>> interceptorMetadataCache;

    private final ConcurrentMap<ClassMetadata<?>, InterceptorMetadata<?>> classMetadataInterceptorMetadataCache;

    private final ConcurrentMap<Class<?>, ClassMetadata<?>> reflectiveClassMetadataCache;

    private boolean unwrapRuntimeExceptions;

    private final BeanManagerImpl manager;

    public DefaultMetadataCachingReader(BeanManagerImpl manager) {
        this.manager = manager;
        this.interceptorMetadataCache = new MapMaker().makeComputingMap(new Function<InterceptorFactory<?>, InterceptorMetadata<?>>() {
            public InterceptorMetadata<?> apply(InterceptorFactory<?> from) {
                return InterceptorMetadataUtils.readMetadataForInterceptorClass(from);
            }
        });

        this.classMetadataInterceptorMetadataCache = new MapMaker().makeComputingMap(new Function<ClassMetadata<?>, InterceptorMetadata<?>>() {
            public InterceptorMetadata<?> apply(ClassMetadata<?> from) {
                return InterceptorMetadataUtils.readMetadataForTargetClass(from);
            }
        });

        this.reflectiveClassMetadataCache = new MapMaker().makeComputingMap(new Function<Class<?>, ClassMetadata<?>>() {
            public ClassMetadata<?> apply(Class<?> from) {
                return ReflectiveClassMetadata.of(from);
            }
        });
        this.unwrapRuntimeExceptions = true;
    }

    public void setUnwrapRuntimeExceptions(boolean unwrapRuntimeExceptions) {
        this.unwrapRuntimeExceptions = unwrapRuntimeExceptions;
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(InterceptorFactory<T> interceptorReference) {
        try {
            return (InterceptorMetadata<T>) interceptorMetadataCache.get(interceptorReference);
        } catch (ComputationException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> TargetClassInterceptorMetadata<T> getTargetClassInterceptorMetadata(ClassMetadata<T> classMetadata) {
        try {
            return (TargetClassInterceptorMetadata<T>) classMetadataInterceptorMetadataCache.get(classMetadata);
        } catch (ComputationException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(Class<T> clazz) {
        try {
            return (InterceptorMetadata<T>) interceptorMetadataCache.get(ClassMetadataInterceptorFactory.of(reflectiveClassMetadataCache.get(clazz), manager));
        } catch (ComputationException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> ClassMetadata<T> getClassMetadata(Class<T> clazz) {
        try {
            return (ClassMetadata<T>) reflectiveClassMetadataCache.get(clazz);
        } catch (ComputationException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public void cleanAfterBoot() {
        classMetadataInterceptorMetadataCache.clear();
    }
}
