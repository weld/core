package org.jboss.weld.annotated.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedField<X> extends BackedAnnotatedMember<X> implements AnnotatedField<X> {

    public static <X, Y extends X> AnnotatedField<X> of(AnnotatedField<X> originalField, AnnotatedType<Y> declaringType) {
        AnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedField<X>(originalField.getBaseType(), originalField.getJavaMember(), downcastDeclaringType);
    }

    public static <X, Y extends X> AnnotatedField<X> of(Field field, AnnotatedType<Y> declaringType) {
        AnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedField<X>(field.getGenericType(), field, downcastDeclaringType);
    }

    private final Field field;

    public BackedAnnotatedField(Type baseType, Field field, AnnotatedType<X> declaringType) {
        super(baseType, declaringType);
        this.field = field;
    }

    public Field getJavaMember() {
        return field;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return field.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(field.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType);
    }

}
