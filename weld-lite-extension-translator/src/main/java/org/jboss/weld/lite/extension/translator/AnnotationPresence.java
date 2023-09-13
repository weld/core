package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

final class AnnotationPresence {

    private AnnotationPresence() {
    }

    static Stream<Annotation> allAnnotations(jakarta.enterprise.inject.spi.AnnotatedType<?> cdiClassDeclaration) {
        Stream<Annotation> classAnnotations = cdiClassDeclaration.getAnnotations().stream();
        Stream<Annotation> fieldAnnotations = cdiClassDeclaration.getFields()
                .stream()
                .flatMap(it -> it.getAnnotations().stream());
        Stream<Annotation> methodAnnotations = cdiClassDeclaration.getMethods()
                .stream()
                .flatMap(it -> it.getAnnotations().stream());
        Stream<Annotation> constructorAnnotations = cdiClassDeclaration.getConstructors()
                .stream()
                .flatMap(it -> it.getAnnotations().stream());
        Stream<Annotation> methodParameterAnnotations = cdiClassDeclaration.getMethods()
                .stream()
                .flatMap(it -> it.getParameters().stream())
                .flatMap(it -> it.getAnnotations().stream());
        Stream<Annotation> constructorParameterAnnotations = cdiClassDeclaration.getConstructors()
                .stream()
                .flatMap(it -> it.getParameters().stream())
                .flatMap(it -> it.getAnnotations().stream());

        // TODO meta-annotations - theoretically, all of the annotations can be declared as meta-annotations as well
        // so we should also recursively search all of the annotations declared by annotations and so on...

        return Stream.concat(
                classAnnotations,
                Stream.concat(
                        fieldAnnotations,
                        Stream.concat(
                                methodAnnotations,
                                Stream.concat(
                                        constructorAnnotations,
                                        Stream.concat(
                                                methodParameterAnnotations,
                                                constructorParameterAnnotations)))));
    }
}
