package org.jboss.weld.lite.extension.translator;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ParameterConfig;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;

class MethodConfigImpl implements MethodConfig {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> configurator;
    private final BeanManager bm;

    MethodConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> configurator,
            BeanManager bm) {
        this.configurator = configurator;
        this.bm = bm;
    }

    @Override
    public MethodInfo info() {
        return new MethodInfoImpl(configurator.getAnnotated(), bm);
    }

    @Override
    public MethodConfig addAnnotation(Class<? extends Annotation> annotationType) {
        configurator.add(AnnotationProxy.create(annotationType, Collections.emptyMap()));
        return this;
    }

    @Override
    public MethodConfig addAnnotation(AnnotationInfo annotation) {
        configurator.add(((AnnotationInfoImpl) annotation).annotation);
        return this;
    }

    @Override
    public MethodConfig addAnnotation(Annotation annotation) {
        configurator.add(annotation);
        return this;
    }

    @Override
    public MethodConfig removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configurator.remove(annotation -> predicate.test(new AnnotationInfoImpl(annotation, bm)));
        return this;
    }

    @Override
    public MethodConfig removeAllAnnotations() {
        configurator.removeAll();
        return this;
    }

    @Override
    public List<ParameterConfig> parameters() {
        return configurator.params()
                .stream()
                .map(annotatedParameterConfigurator -> new ParameterConfigImpl(annotatedParameterConfigurator, bm))
                .collect(Collectors.toList());
    }
}
