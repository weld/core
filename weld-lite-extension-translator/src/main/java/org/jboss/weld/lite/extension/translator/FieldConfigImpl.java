package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.function.Predicate;

class FieldConfigImpl implements FieldConfig {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator;
    private final BeanManager bm;

    FieldConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator,
                    BeanManager bm) {
        this.configurator = configurator;
        this.bm = bm;
    }

    @Override
    public FieldInfo info() {
        return new FieldInfoImpl(configurator.getAnnotated(), bm);
    }

    @Override
    public FieldConfig addAnnotation(Class<? extends Annotation> annotationType) {
        configurator.add(AnnotationProxy.create(annotationType, Collections.emptyMap()));
        return this;
    }

    @Override
    public FieldConfig addAnnotation(AnnotationInfo annotation) {
        configurator.add(((AnnotationInfoImpl) annotation).annotation);
        return this;
    }

    @Override
    public FieldConfig addAnnotation(Annotation annotation) {
        configurator.add(annotation);
        return this;
    }

    @Override
    public FieldConfig removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configurator.remove(annotation -> predicate.test(new AnnotationInfoImpl(annotation, bm)));
        return this;
    }

    @Override
    public FieldConfig removeAllAnnotations() {
        configurator.removeAll();
        return this;
    }
}
