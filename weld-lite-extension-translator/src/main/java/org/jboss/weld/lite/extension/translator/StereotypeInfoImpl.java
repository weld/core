package org.jboss.weld.lite.extension.translator;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.build.compatible.spi.ScopeInfo;
import jakarta.enterprise.inject.build.compatible.spi.StereotypeInfo;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.inject.Named;
import jakarta.inject.Scope;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

class StereotypeInfoImpl implements StereotypeInfo {
    // declaration of the sterotype annotation
    private final jakarta.enterprise.inject.spi.AnnotatedType<? extends Annotation> cdiDeclaration;
    private final BeanManager bm;

    StereotypeInfoImpl(Class<? extends Annotation> stereotypeAnnotation, BeanManager bm) {
        cdiDeclaration = bm.createAnnotatedType(stereotypeAnnotation);
        this.bm = bm;
    }

    @Override
    public ScopeInfo defaultScope() {
        Optional<jakarta.enterprise.inject.spi.AnnotatedType<?>> scopeAnnotation = cdiDeclaration.getAnnotations()
                .stream()
                .filter(it -> it.annotationType().isAnnotationPresent(Scope.class)
                        || it.annotationType().isAnnotationPresent(NormalScope.class))
                .findAny()
                .map(it -> bm.createAnnotatedType(it.annotationType()));

        if (scopeAnnotation.isPresent()) {
            jakarta.enterprise.inject.spi.AnnotatedType<?> scopeType = scopeAnnotation.get();
            boolean isNormal = scopeType.isAnnotationPresent(NormalScope.class);
            return new ScopeInfoImpl(new ClassInfoImpl(scopeType, bm), isNormal);
        }

        return null;
    }

    @Override
    public Collection<AnnotationInfo> interceptorBindings() {
        List<AnnotationInfo> result = new ArrayList<>();
        for (Annotation annotation : cdiDeclaration.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(InterceptorBinding.class)) {
                result.add(new AnnotationInfoImpl(annotation, bm));
            }
        }
        return result;
    }

    @Override
    public boolean isAlternative() {
        return cdiDeclaration.isAnnotationPresent(Alternative.class);
    }

    @Override
    public Integer priority() {
        return cdiDeclaration.isAnnotationPresent(Priority.class)
                ? cdiDeclaration.getAnnotation(Priority.class).value()
                : null;
    }

    @Override
    public boolean isNamed() {
        return cdiDeclaration.isAnnotationPresent(Named.class);
    }
}
