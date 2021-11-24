package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;

final class AnnotationBuilderFactoryImpl implements AnnotationBuilderFactory {
    @Override
    public AnnotationBuilder create(Class<? extends Annotation> annotationType) {
        return new AnnotationBuilderImpl(annotationType);
    }

    @Override
    public AnnotationBuilder create(ClassInfo annotationType) {
        return new AnnotationBuilderImpl((Class<? extends Annotation>) ((ClassInfoImpl) annotationType).cdiDeclaration.getJavaClass());
    }
}
