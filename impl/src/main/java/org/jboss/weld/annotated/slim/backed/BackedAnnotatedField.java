package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.AnnotatedField;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.FieldHolder;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedField<X> extends BackedAnnotatedMember<X> implements AnnotatedField<X>, Serializable {

    public static <X, Y extends X> AnnotatedField<X> of(Field field, BackedAnnotatedType<Y> declaringType,
            SharedObjectCache sharedObjectCache) {
        BackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedField<X>(field.getGenericType(), field, downcastDeclaringType, sharedObjectCache);
    }

    private final Field field;

    public BackedAnnotatedField(Type baseType, Field field, BackedAnnotatedType<X> declaringType,
            SharedObjectCache sharedObjectCache) {
        super(baseType, declaringType, sharedObjectCache);
        this.field = field;
    }

    public Field getJavaMember() {
        return field;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return field.getAnnotation(annotationType);
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return field;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return field.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BackedAnnotatedField<?> other = (BackedAnnotatedField<?>) obj;
        if (field == null) {
            if (other.field != null) {
                return false;
            }
        } else if (!field.equals(other.field)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedField(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<X> extends BackedAnnotatedMemberSerializationProxy<X, AnnotatedField<X>> {

        private static final long serialVersionUID = -8041111397369568219L;

        public SerializationProxy(BackedAnnotatedField<X> field) {
            super(field.getDeclaringType(), new FieldHolder(field.getJavaMember()));
        }

        private Object readResolve() throws ObjectStreamException {
            return resolve();
        }

        @Override
        protected Iterable<AnnotatedField<X>> getCandidates() {
            return cast(type.getFields());
        }
    }
}
