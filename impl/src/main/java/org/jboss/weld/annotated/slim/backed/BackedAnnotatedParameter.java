package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedCallable;
import jakarta.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedParameter<X> extends BackedAnnotated implements AnnotatedParameter<X>, Serializable {

    public static <X> List<AnnotatedParameter<X>> forExecutable(Executable executable,
            BackedAnnotatedCallable<X, ?> declaringCallable, SharedObjectCache cache) {
        final Parameter[] parameters = executable.getParameters();
        if (parameters.length == 0) {
            return Collections.emptyList();
        }
        ImmutableList.Builder<AnnotatedParameter<X>> builder = ImmutableList.builder();
        for (int i = 0; i < parameters.length; i++) {
            builder.add(BackedAnnotatedParameter.of(parameters[i], i, declaringCallable, cache));
        }
        return builder.build();
    }

    public static <X> AnnotatedParameter<X> of(Parameter parameter, int position,
            BackedAnnotatedCallable<X, ?> declaringCallable, SharedObjectCache sharedObjectCache) {
        return new BackedAnnotatedParameter<X>(parameter, position, declaringCallable, sharedObjectCache);
    }

    private final Parameter parameter;
    private final int position;
    private final BackedAnnotatedCallable<X, ?> declaringCallable;

    private BackedAnnotatedParameter(Parameter parameter, int position, BackedAnnotatedCallable<X, ?> declaringCallable,
            SharedObjectCache sharedObjectCache) {
        super(parameter.getParameterizedType(), sharedObjectCache);
        this.parameter = parameter;
        this.position = position;
        this.declaringCallable = declaringCallable;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public Parameter getJavaParameter() {
        return parameter;
    }

    @Override
    public BackedAnnotatedCallable<X, ?> getDeclaringCallable() {
        return declaringCallable;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationType)) {
                return cast(annotation);
            }
        }
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return getReflectionCache().getAnnotations(parameter);
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return null;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
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
        BackedAnnotatedParameter<?> other = (BackedAnnotatedParameter<?>) obj;
        if (parameter == null) {
            if (other.parameter != null) {
                return false;
            }
        } else if (!parameter.equals(other.parameter)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedParameter(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
    }

    private static class SerializationProxy<X> implements Serializable {

        private static final long serialVersionUID = 8784172191880064479L;

        // either BackedAnnotatedMethod or BackedAnnotatedConstructor which are capable of serializing properly
        private final AnnotatedCallable<X> callable;
        private final int position;

        public SerializationProxy(BackedAnnotatedParameter<X> parameter) {
            this.callable = parameter.getDeclaringCallable();
            this.position = parameter.getPosition();
        }

        private Object readResolve() throws ObjectStreamException {
            return callable.getParameters().get(position);
        }
    }

    @Override
    protected ReflectionCache getReflectionCache() {
        return getDeclaringCallable().getDeclaringType().getReflectionCache();
    }
}
