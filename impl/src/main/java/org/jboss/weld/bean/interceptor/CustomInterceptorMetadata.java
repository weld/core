package org.jboss.weld.bean.interceptor;

import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.interceptor.proxy.CustomInterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorReference;
import org.jboss.weld.interceptor.spi.metadata.MethodMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.serialization.spi.helpers.SerializableContextual;

/**
 * @author Marius Bogoevici
 */
public class CustomInterceptorMetadata implements InterceptorMetadata<SerializableContextual<Interceptor<?>, ?>> {


    private SerializableContextualInterceptorReference reference;

    private ClassMetadata<?> classMetadata;

    public CustomInterceptorMetadata(SerializableContextualInterceptorReference serializableContextualInterceptorReference, ClassMetadata<?> classMetadata) {
        this.reference = serializableContextualInterceptorReference;
        this.classMetadata = classMetadata;
    }

    public InterceptorReference<SerializableContextual<Interceptor<?>, ?>> getInterceptorReference() {
       return reference;
    }

    public ClassMetadata<?> getInterceptorClass() {
        return classMetadata;
    }

    public List<MethodMetadata> getInterceptorMethods(InterceptionType interceptionType) {
        return Collections.singletonList(null);
    }

    public boolean isEligible(InterceptionType interceptionType) {
        return reference.getInterceptor().get().intercepts(javax.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }

    public boolean isTargetClass() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public InterceptorInvocation getInterceptorInvocation(Object interceptorInstance, InterceptorMetadata interceptorReference, InterceptionType interceptionType) {
        return new CustomInterceptorInvocation(reference.getInterceptor().get(), interceptorInstance, javax.enterprise.inject.spi.InterceptionType.valueOf(interceptionType.name()));
    }
}
