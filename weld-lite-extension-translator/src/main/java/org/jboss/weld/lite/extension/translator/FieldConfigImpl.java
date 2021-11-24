package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.FieldInfo;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.function.Predicate;

class FieldConfigImpl implements FieldConfig {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator;

    FieldConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator) {
        this.configurator = configurator;
    }

    @Override
    public FieldInfo info() {
        return new FieldInfoImpl(configurator.getAnnotated());
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
        configurator.remove(annotation -> predicate.test(new AnnotationInfoImpl(annotation)));
        return this;
    }

    @Override
    public FieldConfig removeAllAnnotations() {
        configurator.removeAll();
        return this;
    }
}
