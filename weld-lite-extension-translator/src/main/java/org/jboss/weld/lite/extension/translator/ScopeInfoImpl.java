package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.ScopeInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

class ScopeInfoImpl implements ScopeInfo {
    private final ClassInfo annotation;
    private final boolean isNormal;

    ScopeInfoImpl(ClassInfo annotation, boolean isNormal) {
        this.annotation = annotation;
        this.isNormal = isNormal;
    }

    @Override
    public ClassInfo annotation() {
        return annotation;
    }

    @Override
    public boolean isNormal() {
        return isNormal;
    }
}
