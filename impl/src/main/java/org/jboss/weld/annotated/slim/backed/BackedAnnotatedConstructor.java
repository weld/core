package org.jboss.weld.annotated.slim.backed;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.ConstructorHolder;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedConstructor<X> extends BackedAnnotatedCallable<X, Constructor<X>>
        implements AnnotatedConstructor<X>, Serializable {

    public static <X> AnnotatedConstructor<X> of(Constructor<X> constructor, BackedAnnotatedType<X> declaringType,
            SharedObjectCache sharedObjectCache) {
        return new BackedAnnotatedConstructor<X>(constructor, declaringType, sharedObjectCache);
    }

    public BackedAnnotatedConstructor(Constructor<X> constructor, BackedAnnotatedType<X> declaringType,
            SharedObjectCache sharedObjectCache) {
        super(constructor, constructor.getDeclaringClass(), declaringType, sharedObjectCache);
    }

    @Override
    protected List<AnnotatedParameter<X>> initParameters(Constructor<X> member, SharedObjectCache sharedObjectCache) {
        int length = member.getParameterCount();
        if (length == member.getGenericParameterTypes().length && length == member.getParameterAnnotations().length) {
            return BackedAnnotatedParameter.forExecutable(member, this, sharedObjectCache);
        } else {
            /*
             * We are seeing either http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6520205 or
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5087240 or both.
             *
             * It is difficult to detect and adapt to these bugs properly. Therefore, we pretend to see a no-args constructor.
             * Although misleading, it is quite
             * safe to do that since non-static inner classes are not CDI-managed beans anyway and CDI constructor injection
             * into Enums is not supported.
             */
            return Collections.emptyList();
        }

    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedConstructor(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<X> extends BackedAnnotatedMemberSerializationProxy<X, AnnotatedConstructor<X>> {

        private static final long serialVersionUID = -2726172060851333254L;

        public SerializationProxy(BackedAnnotatedConstructor<X> constructor) {
            super(constructor.getDeclaringType(), new ConstructorHolder<X>(constructor.getJavaMember()));
        }

        private Object readResolve() throws ObjectStreamException {
            return resolve();
        }

        @Override
        protected Iterable<AnnotatedConstructor<X>> getCandidates() {
            return type.getConstructors();
        }
    }
}
