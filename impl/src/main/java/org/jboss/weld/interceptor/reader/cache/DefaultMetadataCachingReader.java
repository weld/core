package org.jboss.weld.interceptor.reader.cache;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCacheValue;
import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import org.jboss.weld.interceptor.reader.ClassMetadataInterceptorReference;
import org.jboss.weld.interceptor.reader.InterceptorMetadataUtils;
import org.jboss.weld.interceptor.reader.ReflectiveClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorReference;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 *
 */
public class DefaultMetadataCachingReader implements MetadataCachingReader {
    private final LoadingCache<InterceptorReference<?>, InterceptorMetadata<?>> interceptorMetadataCache;

    private final LoadingCache<ClassMetadata<?>, InterceptorMetadata<?>> classMetadataInterceptorMetadataCache;

    private final LoadingCache<Class<?>, ClassMetadata<?>> reflectiveClassMetadataCache;

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
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(InterceptorReference<T> interceptorReference) {
        return getCastCacheValue(interceptorMetadataCache, interceptorReference);
    }

    public <T> InterceptorMetadata<T> getTargetClassInterceptorMetadata(ClassMetadata<T> classMetadata) {
        return getCastCacheValue(classMetadataInterceptorMetadataCache, classMetadata);
    }

    public <T> InterceptorMetadata<T> getInterceptorMetadata(Class<T> clazz) {
        return getCastCacheValue(interceptorMetadataCache, ClassMetadataInterceptorReference.of(getCacheValue(reflectiveClassMetadataCache, clazz)));
    }

    public <T> ClassMetadata<T> getClassMetadata(Class<T> clazz) {
        return getCastCacheValue(reflectiveClassMetadataCache, clazz);
    }

}
