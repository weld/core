package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.reflection.Formats;

public class UnbackedAnnotatedField<X> extends UnbackedAnnotatedMember<X> implements AnnotatedField<X> {

    public static <X, Y extends X> AnnotatedField<X> of(AnnotatedField<X> originalField, AnnotatedType<Y> declaringType) {
        AnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new UnbackedAnnotatedField<X>(originalField.getBaseType(), originalField.getTypeClosure(), originalField.getAnnotations(), originalField.getJavaMember(),
                downcastDeclaringType);
    }

    private final Field field;

    public UnbackedAnnotatedField(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Field field, AnnotatedType<X> declaringType) {
        super(baseType, typeClosure, annotations, declaringType);
        this.field = field;
    }

    public Field getJavaMember() {
        return field;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedField(this);
    }
}
