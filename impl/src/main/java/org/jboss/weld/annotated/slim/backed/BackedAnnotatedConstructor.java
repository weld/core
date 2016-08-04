package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.collections.WeldCollections.immutableListView;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.ConstructorHolder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR", "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedConstructor<X> extends BackedAnnotatedCallable<X, Constructor<X>> implements AnnotatedConstructor<X>, Serializable {

    public static <X> AnnotatedConstructor<X> of(Constructor<X> constructor, BackedAnnotatedType<X> declaringType, SharedObjectCache sharedObjectCache) {
        return new BackedAnnotatedConstructor<X>(constructor, declaringType, sharedObjectCache);
    }

    private final Constructor<X> constructor;

    public BackedAnnotatedConstructor(Constructor<X> constructor, BackedAnnotatedType<X> declaringType, SharedObjectCache sharedObjectCache) {
        super(constructor, constructor.getDeclaringClass(), declaringType, sharedObjectCache);
        this.constructor = constructor;
    }

    @Override
    protected List<AnnotatedParameter<X>> initParameters(Constructor<X> member, SharedObjectCache sharedObjectCache) {
        final Class<?>[] parameterTypes = member.getParameterTypes();
        final Type[] genericParameterTypes = member.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = member.getParameterAnnotations();

        if (parameterTypes.length == genericParameterTypes.length && genericParameterTypes.length == parameterAnnotations.length) {
            List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(parameterTypes.length);
            int nesting = Reflections.getNesting(getDeclaringType().getJavaClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                int gi = i - nesting;
                Class<?> clazz = parameterTypes[i];

                Type parameterType;
                int position;
                if (member.getGenericParameterTypes().length > gi && gi >= 0) {
                    parameterType = member.getGenericParameterTypes()[gi];
                    position = gi;
                } else {
                    parameterType = clazz;
                    position = i;
                }
                parameters.add(new BackedAnnotatedParameter<X>(parameterType, parameterAnnotations[position], position, this, sharedObjectCache));
            }
            return immutableListView(parameters);
        } else {
            /*
             * We are seeing either http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6520205 or
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5087240 or both.
             *
             * It is difficult to detect and adapt to these bugs properly. Therefore, we pretend to see a no-args constructor.
             * Although misleading, it is quite safe to do that since non-static inner classes are not CDI-managed beans anyway
             * and CDI constructor injection into Enums is not supported.
             */
            return Collections.emptyList();
        }
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return constructor;
    }

    public Constructor<X> getJavaMember() {
        return constructor;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return constructor.getAnnotation(annotationType);
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return constructor.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((constructor == null) ? 0 : constructor.hashCode());
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
        BackedAnnotatedConstructor<?> other = (BackedAnnotatedConstructor<?>) obj;
        if (constructor == null) {
            if (other.constructor != null) {
                return false;
            }
        } else if (!constructor.equals(other.constructor)) {
            return false;
        }
        return true;
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
