package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedField;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Formats;

public class UnbackedAnnotatedField<X> extends UnbackedAnnotatedMember<X> implements AnnotatedField<X>, Serializable {

    public static <X, Y extends X> AnnotatedField<X> of(AnnotatedField<X> originalField, UnbackedAnnotatedType<Y> declaringType) {
        UnbackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new UnbackedAnnotatedField<X>(originalField.getBaseType(), originalField.getTypeClosure(), originalField.getAnnotations(), originalField.getJavaMember(),
                downcastDeclaringType);
    }

    private final Field field;

    public UnbackedAnnotatedField(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Field field, UnbackedAnnotatedType<X> declaringType) {
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

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new UnbackedMemberIdentifier<X>(getDeclaringType(), AnnotatedTypes.createFieldId(this));
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }
}
