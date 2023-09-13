package org.jboss.weld.lite.extension.translator;

import java.util.Collection;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.build.compatible.spi.InterceptorInfo;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;

class InterceptorInfoImpl extends BeanInfoImpl implements InterceptorInfo {
    final jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor;

    InterceptorInfoImpl(jakarta.enterprise.inject.spi.Interceptor<?> cdiInterceptor,
            jakarta.enterprise.inject.spi.Annotated cdiDeclaration, BeanManager bm) {
        super(cdiInterceptor, cdiDeclaration, null, bm);
        this.cdiInterceptor = cdiInterceptor;
    }

    @Override
    public Collection<AnnotationInfo> interceptorBindings() {
        return cdiInterceptor.getInterceptorBindings()
                .stream()
                .map(annotation -> new AnnotationInfoImpl(annotation, bm))
                .collect(Collectors.toList());
    }

    @Override
    public boolean intercepts(jakarta.enterprise.inject.spi.InterceptionType interceptionType) {
        return cdiInterceptor.intercepts(interceptionType);
    }
}
