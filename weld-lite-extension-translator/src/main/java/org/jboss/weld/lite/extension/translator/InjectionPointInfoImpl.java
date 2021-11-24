package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.InjectionPointInfo;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.DeclarationInfo;
import jakarta.enterprise.lang.model.types.Type;
import org.jboss.weld.lite.extension.translator.util.reflection.AnnotatedTypes;

import java.util.Collection;
import java.util.stream.Collectors;

class InjectionPointInfoImpl implements InjectionPointInfo {
    private final jakarta.enterprise.inject.spi.InjectionPoint cdiInjectionPoint;

    InjectionPointInfoImpl(jakarta.enterprise.inject.spi.InjectionPoint cdiInjectionPoint) {
        this.cdiInjectionPoint = cdiInjectionPoint;
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(cdiInjectionPoint.getType()));
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return cdiInjectionPoint.getQualifiers()
                .stream()
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public DeclarationInfo declaration() {
        return DeclarationInfoImpl.fromCdiDeclaration(cdiInjectionPoint.getAnnotated());
    }
}
