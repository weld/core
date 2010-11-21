package org.jboss.weld.annotated.unbacked;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

public class UnbackedAnnotatedType<X> extends UnbackedAnnotated implements AnnotatedType<X> {

    public static <X> AnnotatedType<X> of(AnnotatedType<X> originalType) {
        return new UnbackedAnnotatedType<X>(originalType.getBaseType(), originalType.getTypeClosure(), originalType.getAnnotations(), originalType.getJavaClass(),
                originalType.getConstructors(), originalType.getMethods(), originalType.getFields());
    }

    private final Class<X> javaClass;
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedMethod<? super X>> methods;
    private final Set<AnnotatedField<? super X>> fields;

    public UnbackedAnnotatedType(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Class<X> javaClass, Set<AnnotatedConstructor<X>> originalConstructors,
            Set<AnnotatedMethod<? super X>> originalMethods, Set<AnnotatedField<? super X>> originalFields) {
        super(baseType, typeClosure, annotations);
        this.javaClass = javaClass;
        Set<AnnotatedConstructor<X>> constructors = new HashSet<AnnotatedConstructor<X>>(originalConstructors.size());
        for (AnnotatedConstructor<X> originalConstructor : originalConstructors) {
            constructors.add(UnbackedAnnotatedConstructor.of(originalConstructor, this));
        }
        this.constructors = Collections.unmodifiableSet(constructors);
        Set<AnnotatedMethod<? super X>> methods = new HashSet<AnnotatedMethod<? super X>>(originalMethods.size());
        for (AnnotatedMethod<? super X> originalMethod : originalMethods) {
            methods.add(UnbackedAnnotatedMethod.of(originalMethod, this));
        }
        this.methods = Collections.unmodifiableSet(methods);
        Set<AnnotatedField<? super X>> fields = new HashSet<AnnotatedField<? super X>>(originalFields.size());
        for (AnnotatedField<? super X> originalField : originalFields) {
            fields.add(UnbackedAnnotatedField.of(originalField, this));
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

}
