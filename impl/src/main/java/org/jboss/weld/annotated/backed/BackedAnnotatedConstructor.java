package org.jboss.weld.annotated.backed;

import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedConstructor<X> extends BackedAnnotatedMember<X> implements AnnotatedConstructor<X> {

    public static <X> AnnotatedConstructor<X> of(AnnotatedConstructor<X> originalConstructor, AnnotatedType<X> declaringType) {
        return new BackedAnnotatedConstructor<X>(originalConstructor.getBaseType(), declaringType, originalConstructor.getParameters(), originalConstructor.getJavaMember());
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public BackedAnnotatedConstructor(Type baseType, AnnotatedType<X> declaringType, List<AnnotatedParameter<X>> originalParameters, Constructor<X> constructor) {
        super(baseType, declaringType);
        this.constructor = constructor;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new BackedAnnotatedParameter<X>(originalParameter.getBaseType(), originalParameter.getAnnotations(), originalParameter.getPosition(), this));
        }
        this.parameters = unmodifiableList(parameters);
    }

    public Constructor<X> getJavaMember() {
        return constructor;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return constructor.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(constructor.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return constructor.isAnnotationPresent(annotationType);
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
    }

}
