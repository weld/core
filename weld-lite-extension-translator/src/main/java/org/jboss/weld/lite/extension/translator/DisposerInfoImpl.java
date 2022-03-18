package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.DisposerInfo;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;

class DisposerInfoImpl implements DisposerInfo {
    private final jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration;
    private final BeanManager bm;

    DisposerInfoImpl(jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration, BeanManager bm) {
        this.cdiDeclaration = cdiDeclaration;
        this. bm = bm;
    }

    @Override
    public MethodInfo disposerMethod() {
        return new MethodInfoImpl(cdiDeclaration.getDeclaringCallable(), bm);
    }

    @Override
    public ParameterInfo disposedParameter() {
        return new ParameterInfoImpl(cdiDeclaration, bm);
    }
}
