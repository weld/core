package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilder;
import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

final class AnnotationBuilderFactoryImpl implements AnnotationBuilderFactory {

    private final BeanManager bm;

    public AnnotationBuilderFactoryImpl(BeanManager bm) {
        this.bm = bm;
    }

    @Override
    public AnnotationBuilder create(Class<? extends Annotation> annotationType) {
        return new AnnotationBuilderImpl(annotationType, bm);
    }

    @Override
    public AnnotationBuilder create(ClassInfo annotationType) {
        return new AnnotationBuilderImpl(
                (Class<? extends Annotation>) ((ClassInfoImpl) annotationType).cdiDeclaration.getJavaClass(), bm);
    }
}
