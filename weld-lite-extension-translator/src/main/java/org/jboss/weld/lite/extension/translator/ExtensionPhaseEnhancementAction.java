package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;

class ExtensionPhaseEnhancementAction {
    private final Set<Class<?>> types;
    private final boolean withSubtypes;
    private final Set<Class<? extends Annotation>> withAnnotations;
    private final Consumer<jakarta.enterprise.inject.spi.ProcessAnnotatedType<?>> acceptor;

    ExtensionPhaseEnhancementAction(Set<Class<?>> types, boolean withSubtypes, Set<Class<? extends Annotation>> withAnnotations,
                                    Consumer<jakarta.enterprise.inject.spi.ProcessAnnotatedType<?>> acceptor) {
        this.types = types;
        this.withSubtypes = withSubtypes;
        this.withAnnotations = withAnnotations;
        this.acceptor = acceptor;
    }

    void run(jakarta.enterprise.inject.spi.ProcessAnnotatedType<?> pat) {
        if (satisfies(pat.getAnnotatedType())) {
            acceptor.accept(pat);
        }
    }

    private boolean satisfies(jakarta.enterprise.inject.spi.AnnotatedType<?> inspectedAnnotatedType) {
        Class<?> inspectedClass = inspectedAnnotatedType.getJavaClass();
        if (types.contains(inspectedClass)) {
            return satisfiesAnnotationConstraints(inspectedAnnotatedType);
        } else if (withSubtypes) {
            boolean typeMatches = false;
            for (Class<?> type : types) {
                if (type.isAssignableFrom(inspectedClass)) {
                    typeMatches = true;
                    break;
                }
            }
            return typeMatches && satisfiesAnnotationConstraints(inspectedAnnotatedType);
        } else {
            return false;
        }
    }

    private boolean satisfiesAnnotationConstraints(jakarta.enterprise.inject.spi.AnnotatedType<?> annotatedType) {
        if (withAnnotations.isEmpty()) {
            return true;
        }

        return AnnotationPresence.allAnnotations(annotatedType)
                .anyMatch(it -> {
                    if (withAnnotations.contains(Annotation.class)) {
                        return true;
                    }
                    if (withAnnotations.contains(it.annotationType())) {
                        return true;
                    }
                    return false;
                });
    }
}
