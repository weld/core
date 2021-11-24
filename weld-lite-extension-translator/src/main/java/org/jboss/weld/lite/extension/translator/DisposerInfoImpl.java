package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.DisposerInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;

class DisposerInfoImpl implements DisposerInfo {
    private final jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration;

    DisposerInfoImpl(jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration) {
        this.cdiDeclaration = cdiDeclaration;
    }

    @Override
    public MethodInfo disposerMethod() {
        return new MethodInfoImpl(cdiDeclaration.getDeclaringCallable());
    }

    @Override
    public ParameterInfo disposedParameter() {
        return new ParameterInfoImpl(cdiDeclaration);
    }
}
