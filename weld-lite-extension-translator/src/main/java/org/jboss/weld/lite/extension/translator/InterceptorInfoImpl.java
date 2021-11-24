package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.InterceptorInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;

import java.util.Collection;
import java.util.stream.Collectors;

class InterceptorInfoImpl extends BeanInfoImpl implements InterceptorInfo {
    final jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor;

    InterceptorInfoImpl(jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor,
            jakarta.enterprise.inject.spi.Annotated cdiDeclaration) {
        super(cdiInterceptor, cdiDeclaration, null);
        this.cdiInterceptor = cdiInterceptor;
    }

    @Override
    public Collection<AnnotationInfo> interceptorBindings() {
        return cdiInterceptor.getInterceptorBindings()
                .stream()
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean intercepts(jakarta.enterprise.inject.spi.InterceptionType interceptionType) {
        return cdiInterceptor.intercepts(interceptionType);
    }
}
