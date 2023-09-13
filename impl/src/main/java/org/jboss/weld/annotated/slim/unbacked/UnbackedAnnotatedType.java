package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
@SuppressFBWarnings(value = { "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class UnbackedAnnotatedType<X> extends UnbackedAnnotated implements SlimAnnotatedType<X>, Serializable {

    public static <X> UnbackedAnnotatedType<X> additionalAnnotatedType(String contextId, AnnotatedType<X> source, String bdaId,
            String suffix, SharedObjectCache cache) {
        return new UnbackedAnnotatedType<X>(source,
                AnnotatedTypeIdentifier.of(contextId, bdaId, source.getJavaClass().getName(), suffix, false), cache);
    }

    public static <X> UnbackedAnnotatedType<X> modifiedAnnotatedType(SlimAnnotatedType<X> originalType, AnnotatedType<X> source,
            SharedObjectCache cache) {
        AnnotatedTypeIdentifier identifier = AnnotatedTypeIdentifier.forModifiedAnnotatedType(originalType.getIdentifier());
        return new UnbackedAnnotatedType<X>(source, identifier, cache);
    }

    private final Class<X> javaClass;
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedMethod<? super X>> methods;
    private final Set<AnnotatedField<? super X>> fields;
    private final AnnotatedTypeIdentifier identifier;

    private UnbackedAnnotatedType(AnnotatedType<X> source, AnnotatedTypeIdentifier identifier, SharedObjectCache cache) {
        super(source.getBaseType(), source.getTypeClosure(), source.getAnnotations());
        this.javaClass = source.getJavaClass();
        ImmutableSet.Builder<AnnotatedConstructor<X>> constructors = ImmutableSet.builder();
        for (AnnotatedConstructor<X> constructor : source.getConstructors()) {
            constructors.add(UnbackedAnnotatedConstructor.of(constructor, this, cache));
        }
        this.constructors = constructors.build();
        ImmutableSet.Builder<AnnotatedMethod<? super X>> methods = ImmutableSet.builder();
        for (AnnotatedMethod<? super X> originalMethod : source.getMethods()) {
            methods.add(UnbackedAnnotatedMethod.of(originalMethod, this, cache));
        }
        this.methods = methods.build();
        ImmutableSet.Builder<AnnotatedField<? super X>> fields = ImmutableSet.builder();
        for (AnnotatedField<? super X> originalField : source.getFields()) {
            fields.add(UnbackedAnnotatedField.of(originalField, this, cache));
        }
        this.fields = fields.build();
        this.identifier = identifier;
    }

    @Override
    public Class<X> getJavaClass() {
        return javaClass;
    }

    @Override
    public Set<AnnotatedConstructor<X>> getConstructors() {
        return constructors;
    }

    @Override
    public Set<AnnotatedMethod<? super X>> getMethods() {
        return methods;
    }

    @Override
    public Set<AnnotatedField<? super X>> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedType(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(getIdentifier());
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    @Override
    public void clear() {
        // noop
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UnbackedAnnotatedType<?>) {
            UnbackedAnnotatedType<?> that = cast(obj);
            return Objects.equals(this.identifier, that.identifier);
        }
        return false;
    }

    @Override
    public AnnotatedTypeIdentifier getIdentifier() {
        return identifier;
    }
}
