package org.jboss.weld.interceptor.reader;

import java.util.function.Function;

import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.InterceptorImpl;
import org.jboss.weld.bean.interceptor.CustomInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.cache.ComputingCache;
import org.jboss.weld.util.cache.ComputingCacheBuilder;

/**
 * InterceptorMetadata reader. The reader produces InterceptorMetadata instances for plain interceptors, CDI interceptors and
 * components' target classes.
 * <p>
 * This class is thread-safe.
 *
 * @author Jozef Hartinger
 *
 */
public class InterceptorMetadataReader {

    private final BeanManagerImpl manager;
    private final ComputingCache<Class<?>, InterceptorClassMetadata<?>> plainInterceptorMetadataCache;
    private final ComputingCache<Interceptor<?>, InterceptorClassMetadata<?>> cdiInterceptorMetadataCache;
    private final Function<Interceptor<?>, InterceptorClassMetadata<?>> interceptorToInterceptorMetadataFunction;

    public InterceptorMetadataReader(final BeanManagerImpl manager) {
        this.manager = manager;
        final ComputingCacheBuilder cacheBuilder = ComputingCacheBuilder.newBuilder();

        this.plainInterceptorMetadataCache = cacheBuilder.build(new Function<Class<?>, InterceptorClassMetadata<?>>() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public InterceptorClassMetadata<?> apply(Class<?> key) {
                EnhancedAnnotatedType<?> type = manager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(key,
                        manager.getId());
                InterceptorFactory<?> factory = PlainInterceptorFactory.of(key, manager);
                return new InterceptorMetadataImpl(key, factory, InterceptorMetadataUtils.buildMethodMap(type, false, manager));
            }
        });

        this.cdiInterceptorMetadataCache = cacheBuilder.build(new Function<Interceptor<?>, InterceptorClassMetadata<?>>() {
            @Override
            public InterceptorClassMetadata<?> apply(Interceptor<?> key) {
                return CustomInterceptorMetadata.of(key);
            }
        });
        this.interceptorToInterceptorMetadataFunction = InterceptorMetadataReader.this::getCdiInterceptorMetadata;
    }

    public <T> InterceptorClassMetadata<T> getPlainInterceptorMetadata(Class<T> clazz) {
        return plainInterceptorMetadataCache.getCastValue(clazz);
    }

    public <T> TargetClassInterceptorMetadata getTargetClassInterceptorMetadata(EnhancedAnnotatedType<T> type) {
        return TargetClassInterceptorMetadata.of(InterceptorMetadataUtils.buildMethodMap(type, true, manager));
    }

    public <T> InterceptorClassMetadata<T> getCdiInterceptorMetadata(Interceptor<T> interceptor) {
        if (interceptor instanceof InterceptorImpl) {
            InterceptorImpl<T> interceptorImpl = (InterceptorImpl<T>) interceptor;
            return interceptorImpl.getInterceptorMetadata();
        }
        return cdiInterceptorMetadataCache.getCastValue(interceptor);
    }

    public Function<Interceptor<?>, InterceptorClassMetadata<?>> getInterceptorToInterceptorMetadataFunction() {
        return interceptorToInterceptorMetadataFunction;
    }

    public void cleanAfterBoot() {
        plainInterceptorMetadataCache.clear();
        cdiInterceptorMetadataCache.clear();
    }
}
