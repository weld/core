package org.jboss.weld.annotated.backed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedType<X> extends BackedAnnotated implements AnnotatedType<X> {

    public static <X> AnnotatedType<X> of(AnnotatedType<X> originalType) {
        return new BackedAnnotatedType<X>(originalType.getBaseType(), originalType.getJavaClass(), originalType.getConstructors(), originalType.getMethods(),
                originalType.getFields());
    }

    private final Class<X> javaClass;
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedMethod<? super X>> methods;
    private final Set<AnnotatedField<? super X>> fields;

    public BackedAnnotatedType(Type baseType, Class<X> javaClass, Set<AnnotatedConstructor<X>> originalConstructors, Set<AnnotatedMethod<? super X>> originalMethods,
            Set<AnnotatedField<? super X>> originalFields) {
        super(baseType);
        this.javaClass = javaClass;
        // Rebuild members to fix up back-refs
        Set<AnnotatedConstructor<X>> constructors = new HashSet<AnnotatedConstructor<X>>(originalConstructors.size());
        for (AnnotatedConstructor<X> originalConstructor : originalConstructors) {
            constructors.add(BackedAnnotatedConstructor.of(originalConstructor, this));
        }
        this.constructors = Collections.unmodifiableSet(constructors);
        Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>(originalMethods.size());
        for (AnnotatedMethod<? super X> originalMethod : originalMethods) {
            methods.add(BackedAnnotatedMethod.of(originalMethod, this));
        }
        this.methods = Collections.unmodifiableSet(methods);
        Set<AnnotatedField<? super X>> fields = new HashSet<AnnotatedField<? super X>>(originalFields.size());
        for (AnnotatedField<? super X> originalField : originalFields) {
            fields.add(BackedAnnotatedField.of(originalField, this));
        }
        this.fields = Collections.unmodifiableSet(fields);
    }

    public Class<X> getJavaClass() {
        return javaClass;
    }

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return constructors;
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return methods;
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return fields;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return javaClass.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(javaClass.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return javaClass.isAnnotationPresent(annotationType);
    }

}
