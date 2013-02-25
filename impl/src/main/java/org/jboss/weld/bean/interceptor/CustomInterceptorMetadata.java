package org.jboss.weld.bean.interceptor;

import org.jboss.weld.interceptor.proxy.CustomInterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;

/**
 * @author Marius Bogoevici
 */
public class CustomInterceptorMetadata<T> implements InterceptorMetadata<T> {

    private CdiInterceptorFactory<T> factory;

    private ClassMetadata<?> classMetadata;

    public CustomInterceptorMetadata(CdiInterceptorFactory<T> factory, ClassMetadata<?> classMetadata) {
        this.factory = factory;
        this.classMetadata = classMetadata;
    }

    public CdiInterceptorFactory<T> getInterceptorFactory() {
       return factory;
    }

    public ClassMetadata<?> getInterceptorClass() {
        return classMetadata;
    }

    public boolean isEligible(InterceptionType interceptionType) {
        return factory.getInterceptor().intercepts(javax.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public InterceptorInvocation getInterceptorInvocation(Object interceptorInstance, InterceptionType interceptionType) {
        return new CustomInterceptorInvocation<T>(factory.getInterceptor(), (T) interceptorInstance, javax.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }

    @Override
    public String toString() {
        return "CustomInterceptorMetadata [" + getInterceptorClass().getClassName() + "]";
    }
}
