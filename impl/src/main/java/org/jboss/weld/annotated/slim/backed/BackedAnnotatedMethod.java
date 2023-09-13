package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Method;

import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.MethodHolder;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedMethod<X> extends BackedAnnotatedCallable<X, Method> implements AnnotatedMethod<X>, Serializable {

    public static <X, Y extends X> AnnotatedMethod<X> of(Method method, BackedAnnotatedType<Y> declaringType,
            SharedObjectCache sharedObjectCache) {
        BackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedMethod<X>(method, downcastDeclaringType, sharedObjectCache);
    }

    public BackedAnnotatedMethod(Method method, BackedAnnotatedType<X> declaringType, SharedObjectCache sharedObjectCache) {
        super(method, method.getGenericReturnType(), declaringType, sharedObjectCache);
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedMethod(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<X> extends BackedAnnotatedMemberSerializationProxy<X, AnnotatedMethod<X>> {

        private static final long serialVersionUID = 8008578690970722095L;

        public SerializationProxy(BackedAnnotatedMethod<X> method) {
            super(method.getDeclaringType(), MethodHolder.of(method));
        }

        private Object readResolve() throws ObjectStreamException {
            return resolve();
        }

        @Override
        protected Iterable<AnnotatedMethod<X>> getCandidates() {
            return cast(type.getMethods());
        }
    }
}
