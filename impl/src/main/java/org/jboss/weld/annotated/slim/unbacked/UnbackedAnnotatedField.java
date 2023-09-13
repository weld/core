package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedField;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class UnbackedAnnotatedField<X> extends UnbackedAnnotatedMember<X> implements AnnotatedField<X>, Serializable {

    public static <X, Y extends X> AnnotatedField<X> of(AnnotatedField<X> originalField, UnbackedAnnotatedType<Y> declaringType,
            SharedObjectCache cache) {
        UnbackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new UnbackedAnnotatedField<X>(originalField.getBaseType(), originalField.getTypeClosure(),
                cache.getSharedSet(originalField.getAnnotations()), originalField.getJavaMember(),
                downcastDeclaringType);
    }

    private final Field field;

    public UnbackedAnnotatedField(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, Field field,
            UnbackedAnnotatedType<X> declaringType) {
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
        throw BeanLogger.LOG.serializationProxyRequired();
    }
}
