package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.function.Predicate;

class ParameterConfigImpl implements ParameterConfig {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator<?> configurator;

    ParameterConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedParameterConfigurator<?> configurator) {
        this.configurator = configurator;
    }

    @Override
    public ParameterInfo info() {
        return new ParameterInfoImpl(configurator.getAnnotated());
    }

    @Override
    public ParameterConfig addAnnotation(Class<? extends Annotation> annotationType) {
        configurator.add(AnnotationProxy.create(annotationType, Collections.emptyMap()));
        return this;
    }

    @Override
    public ParameterConfig addAnnotation(AnnotationInfo annotation) {
        configurator.add(((AnnotationInfoImpl) annotation).annotation);
        return this;
    }

    @Override
    public ParameterConfig addAnnotation(Annotation annotation) {
        configurator.add(annotation);
        return this;
    }

    @Override
    public ParameterConfig removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configurator.remove(annotation -> predicate.test(new AnnotationInfoImpl(annotation)));
        return this;
    }

    @Override
    public ParameterConfig removeAllAnnotations() {
        configurator.removeAll();
        return this;
    }
}
