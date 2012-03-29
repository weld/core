package org.jboss.weld.annotated.backed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedType<X> extends BackedAnnotated implements AnnotatedType<X> {

    public static <X> AnnotatedType<X> of(AnnotatedType<X> originalType) {
        return new BackedAnnotatedType<X>(originalType.getBaseType(), originalType.getJavaClass(), originalType.getConstructors(), originalType.getMethods(),
                originalType.getFields());
    }

    public static <X> AnnotatedType<X> of(Class<X> javaClass) {
        return new BackedAnnotatedType<X>(javaClass);
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

    public BackedAnnotatedType(Class<X> javaClass) {
        super(javaClass);
        this.javaClass = javaClass;
        // TODO this all should be initialized lazily so that we can serialize the AnnotatedType

        Constructor<?>[] declaredConstructors = SecureReflections.getDeclaredConstructors(javaClass);
        ArraySet<AnnotatedConstructor<X>> constructors = new ArraySet<AnnotatedConstructor<X>>(declaredConstructors.length);
        ArraySet<AnnotatedMethod<? super X>> methods = new ArraySet<AnnotatedMethod<? super X>>();
        ArraySet<AnnotatedField<? super X>> fields = new ArraySet<AnnotatedField<? super X>>();

        for (Constructor<?> constructor : declaredConstructors) {
            Constructor<X> c = Reflections.cast(constructor);
            constructors.add(BackedAnnotatedConstructor.of(c, this));
        }
        Class<? super X> clazz = javaClass;
        while (clazz != Object.class && clazz != null) {
            for (Method method : SecureReflections.getDeclaredMethods(clazz)) {
                methods.add(BackedAnnotatedMethod.of(method, this));
            }
            for (Field field : SecureReflections.getDeclaredFields(clazz)) {
                fields.add(BackedAnnotatedField.of(field, this));
            }
            clazz = clazz.getSuperclass();
        }
        this.constructors = Collections.unmodifiableSet(constructors.trimToSize());
        this.methods = Collections.unmodifiableSet(methods.trimToSize());
        this.fields = Collections.unmodifiableSet(fields.trimToSize());
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
