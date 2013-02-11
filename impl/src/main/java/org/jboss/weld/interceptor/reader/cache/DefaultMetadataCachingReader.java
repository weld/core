package org.jboss.weld.interceptor.reader.cache;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorFactory;
import org.jboss.weld.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.weld.interceptor.reader.ReflectiveClassMetadata;
import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.manager.BeanManagerImpl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;

/**
 *
 */
public class DefaultMetadataCachingReader implements MetadataCachingReader {

    private final LoadingCache<InterceptorFactory<?>, InterceptorMetadata<?>> interceptorMetadataCache;

    private final LoadingCache<ClassMetadata<?>, InterceptorMetadata<?>> classMetadataInterceptorMetadataCache;

    private final LoadingCache<Class<?>, ClassMetadata<?>> reflectiveClassMetadataCache;

    private boolean unwrapRuntimeExceptions;

    private final BeanManagerImpl manager;

    public DefaultMetadataCachingReader(BeanManagerImpl manager) {
        this.manager = manager;
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        this.interceptorMetadataCache = cacheBuilder.build(new CacheLoader<InterceptorFactory<?>, InterceptorMetadata<?>>() {
            public InterceptorMetadata<?> load(InterceptorFactory<?> from) {
                return InterceptorMetadataUtils.readMetadataForInterceptorClass(from);
            }
        });

        this.classMetadataInterceptorMetadataCache = cacheBuilder
                .build(new CacheLoader<ClassMetadata<?>, InterceptorMetadata<?>>() {
                    public InterceptorMetadata<?> load(ClassMetadata<?> from) {
                return InterceptorMetadataUtils.readMetadataForTargetClass(from);
            }
        });

        this.reflectiveClassMetadataCache = cacheBuilder.build(new CacheLoader<Class<?>, ClassMetadata<?>>() {
            public ClassMetadata<?> load(Class<?> from) {
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
            return getCastCacheValue(interceptorMetadataCache, interceptorReference, false);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> TargetClassInterceptorMetadata<T> getTargetClassInterceptorMetadata(ClassMetadata<T> classMetadata) {
        try {
            return getCastCacheValue(classMetadataInterceptorMetadataCache, classMetadata, false);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(Class<T> clazz) {
        try {
            return getCastCacheValue(interceptorMetadataCache,
                    ClassMetadataInterceptorFactory.of(getCacheValue(reflectiveClassMetadataCache, clazz, false), manager), false);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public <T> ClassMetadata<T> getClassMetadata(Class<T> clazz) {
        try {
            return getCastCacheValue(reflectiveClassMetadataCache, clazz, false);
        } catch (UncheckedExecutionException e) {
            if (unwrapRuntimeExceptions && e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }

    public void cleanAfterBoot() {
        classMetadataInterceptorMetadataCache.invalidateAll();
    }
}
