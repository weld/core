package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.AnnotatedTypes;
import org.jboss.weld.util.reflection.Formats;

/**
 * Wrapper for extension-provided {@link AnnotatedType}. This may seem unnecessary, however it does mean we are providing a
 * consistent view for debugging, error reporting etc. This implementation is also serializable no matter if the original
 * extension-provided {@link AnnotatedType} implementation is.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 *
 * @param <X> the type
 */
public class UnbackedAnnotatedType<X> extends UnbackedAnnotated implements SlimAnnotatedType<X>, Serializable {

    public static <X> UnbackedAnnotatedType<X> of(AnnotatedType<X> originalType) {
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

    @Override
    public String toString() {
        return Formats.formatAnnotatedType(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<X> implements Serializable {

        private static final long serialVersionUID = 402976292268601274L;
        private final String id;

        public SerializationProxy(UnbackedAnnotatedType<X> annotatedType) {
            this.id = AnnotatedTypes.createTypeId(annotatedType);
        }

        private Object readResolve() {
            return Container.instance().services().get(ClassTransformer.class).getUnbackedAnnotatedType(id);
        }
    }
}
