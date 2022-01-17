package org.jboss.weld.lite.extension.translator;

import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ClassConfigImpl implements ClassConfig {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<?> configurator;
    private final BeanManager bm;

    ClassConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<?> configurator,
                    BeanManager bm) {
        this.configurator = configurator;
        this.bm = bm;
    }

    @Override
    public ClassInfo info() {
        return new ClassInfoImpl(configurator.getAnnotated(), bm);
    }

    @Override
    public ClassConfig addAnnotation(Class<? extends Annotation> annotationType) {
        configurator.add(AnnotationProxy.create(annotationType, Collections.emptyMap()));
        return this;
    }

    @Override
    public ClassConfig addAnnotation(AnnotationInfo annotation) {
        configurator.add(((AnnotationInfoImpl) annotation).annotation);
        return this;
    }

    @Override
    public ClassConfig addAnnotation(Annotation annotation) {
        configurator.add(annotation);
        return this;
    }

    @Override
    public ClassConfig removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configurator.remove(annotation -> predicate.test(new AnnotationInfoImpl(annotation, bm)));
        return this;
    }

    @Override
    public ClassConfig removeAllAnnotations() {
        configurator.removeAll();
        return this;
    }

    @Override
    public Collection<MethodConfig> constructors() {
        return configurator.constructors()
                .stream()
                .map(annotatedConstructorConfigurator -> new MethodConstructorConfigImpl(annotatedConstructorConfigurator, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<MethodConfig> methods() {
        return configurator.methods()
                .stream()
                .map(annotatedMethodConfigurator -> new MethodConfigImpl(annotatedMethodConfigurator, bm))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FieldConfig> fields() {
        return configurator.fields()
                .stream()
                .map(annotatedFieldConfigurator -> new FieldConfigImpl(annotatedFieldConfigurator, bm))
                .collect(Collectors.toList());
    }
}
