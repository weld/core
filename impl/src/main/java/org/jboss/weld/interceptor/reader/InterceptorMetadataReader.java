package org.jboss.weld.interceptor.reader;

import static org.jboss.weld.util.cache.LoadingCacheUtils.getCastCacheValue;

import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * InterceptorMetadata reader. The reader produces InterceptorMetadata instances for plain interceptors, CDI interceptors and components' target classes.
 * <p>
 * This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 */
public class InterceptorMetadataReader {

    private final BeanManagerImpl manager;
    private final LoadingCache<Class<?>, InterceptorClassMetadata<?>> plainInterceptorMetadataCache;
    private final LoadingCache<Interceptor<?>, InterceptorClassMetadata<?>> cdiInterceptorMetadataCache;

    public InterceptorMetadataReader(final BeanManagerImpl manager) {
        this.manager = manager;
        final CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        this.plainInterceptorMetadataCache = cacheBuilder.build(new CacheLoader<Class<?>, InterceptorClassMetadata<?>>() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public InterceptorClassMetadata<?> load(Class<?> key) throws Exception {
                EnhancedAnnotatedType<?> type = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(key, manager.getId());
                InterceptorFactory<?> factory = PlainInterceptorFactory.of(key, manager);
                return new InterceptorMetadataImpl(key, factory, InterceptorMetadataUtils.buildMethodMap(type, false, manager));
            }
        });

        this.cdiInterceptorMetadataCache = cacheBuilder.build(new CacheLoader<Interceptor<?>, InterceptorClassMetadata<?>>() {
            @Override
            public InterceptorClassMetadata<?> load(Interceptor<?> key) throws Exception {
                return CustomInterceptorMetadata.of(key);
            }
        });
    }

    public <T> InterceptorClassMetadata<T> getPlainInterceptorMetadata(Class<T> clazz) {
        return getCastCacheValue(plainInterceptorMetadataCache, clazz);
    }

    public <T> TargetClassInterceptorMetadata getTargetClassInterceptorMetadata(EnhancedAnnotatedType<T> type) {
        return TargetClassInterceptorMetadata.of(InterceptorMetadataUtils.buildMethodMap(type, true, manager));
    }

    public <T> InterceptorClassMetadata<T> getCdiInterceptorMetadata(Interceptor<T> interceptor) {
        if (interceptor instanceof InterceptorImpl) {
            InterceptorImpl<T> interceptorImpl = (InterceptorImpl<T>) interceptor;
            return interceptorImpl.getInterceptorMetadata();
        }
        return getCastCacheValue(cdiInterceptorMetadataCache, interceptor);
    }

    public void cleanAfterBoot() {
        plainInterceptorMetadataCache.invalidateAll();
        plainInterceptorMetadataCache.cleanUp();
        cdiInterceptorMetadataCache.invalidateAll();
        cdiInterceptorMetadataCache.cleanUp();
    }
}
