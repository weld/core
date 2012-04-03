package org.jboss.weld.annotated.slim.backed;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedType<X> extends BackedAnnotated implements SlimAnnotatedType<X> {

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass) {
        return new BackedAnnotatedType<X>(javaClass, javaClass);
    }

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, Type baseType) {
        return new BackedAnnotatedType<X>(javaClass, baseType);
    }

    private final Class<X> javaClass;
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedMethod<? super X>> methods;
    private final Set<AnnotatedField<? super X>> fields;

    public BackedAnnotatedType(Class<X> rawType, Type baseType) {
        super(baseType);
        this.javaClass = rawType;
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

    @Override
    public String toString() {
        return Formats.formatAnnotatedType(this);
    }
}
