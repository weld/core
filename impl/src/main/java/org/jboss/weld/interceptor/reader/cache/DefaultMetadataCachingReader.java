package org.jboss.weld.interceptor.reader.cache;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorReference;
import org.jboss.weld.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.weld.interceptor.reader.ReflectiveClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorReference;

/**
 *
 */
public class DefaultMetadataCachingReader implements MetadataCachingReader {
    private final LoadingCache<InterceptorReference<?>, InterceptorMetadata<?>> interceptorMetadataCache;

    private final LoadingCache<ClassMetadata<?>, InterceptorMetadata<?>> classMetadataInterceptorMetadataCache;

    private final LoadingCache<Class<?>, ClassMetadata<?>> reflectiveClassMetadataCache;

    private boolean unwrapRuntimeExceptions;

    public DefaultMetadataCachingReader() {
        this.interceptorMetadataCache = CacheBuilder.newBuilder().build(new CacheLoader<InterceptorReference<?>, InterceptorMetadata<?>>() {
            public InterceptorMetadata<?> load(InterceptorReference<?> from) {
                return InterceptorMetadataUtils.readMetadataForInterceptorClass(from);
            }
        });

        this.classMetadataInterceptorMetadataCache = CacheBuilder.newBuilder().build(new CacheLoader<ClassMetadata<?>, InterceptorMetadata<?>>() {
            public InterceptorMetadata<?> load(ClassMetadata<?> from) {
                return InterceptorMetadataUtils.readMetadataForTargetClass(from);
            }
        });

        this.reflectiveClassMetadataCache = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, ClassMetadata<?>>() {
            public ClassMetadata<?> load(Class<?> from) {
                return ReflectiveClassMetadata.of(from);
            }
        });
        this.unwrapRuntimeExceptions = true;
    }

    public void setUnwrapRuntimeExceptions(boolean unwrapRuntimeExceptions) {
        this.unwrapRuntimeExceptions = unwrapRuntimeExceptions;
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(InterceptorReference<T> interceptorReference) {
        try {
            return getCastCacheValue(interceptorMetadataCache, interceptorReference);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> InterceptorMetadata<T> getTargetClassInterceptorMetadata(ClassMetadata<T> classMetadata) {
        try {
            return getCastCacheValue(classMetadataInterceptorMetadataCache, classMetadata);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(Class<T> clazz) {
        try {
            return getCastCacheValue(interceptorMetadataCache, ClassMetadataInterceptorReference.of(getCacheValue(reflectiveClassMetadataCache, clazz)));
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> ClassMetadata<T> getClassMetadata(Class<T> clazz) {
        try {
            return getCastCacheValue(reflectiveClassMetadataCache, clazz);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }


}
