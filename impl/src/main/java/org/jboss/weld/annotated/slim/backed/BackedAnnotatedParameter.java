package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.resources.SharedObjectFacade;
import org.jboss.weld.util.reflection.Formats;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedParameter<X> extends BackedAnnotated implements AnnotatedParameter<X>, Serializable {

    public static <X> AnnotatedParameter<X> of(Type baseType, Annotation[] annotations, int position, AnnotatedCallable<X> declaringCallable, SharedObjectCache cache) {
        return new BackedAnnotatedParameter<X>(baseType, annotations, position, declaringCallable, cache);
    }

    private final int position;
    private final AnnotatedCallable<X> declaringCallable;
    private transient Set<Annotation> annotations;

    public BackedAnnotatedParameter(Type baseType, Annotation[] annotations, int position, AnnotatedCallable<X> declaringCallable, SharedObjectCache cache) {
        super(baseType, cache);
        this.position = position;
        this.declaringCallable = declaringCallable;
        this.annotations = SharedObjectFacade.wrap(ImmutableSet.copyOf(annotations));
    }

    public int getPosition() {
        return position;
    }

    public AnnotatedCallable<X> getDeclaringCallable() {
        return declaringCallable;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationType)) {
                return cast(annotation);
            }
        }
        return null;
    }

    public Set<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((declaringCallable == null) ? 0 : declaringCallable.hashCode());
        result = prime * result + position;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BackedAnnotatedParameter<?> other = (BackedAnnotatedParameter<?>) obj;
        if (declaringCallable == null) {
            if (other.declaringCallable != null)
                return false;
        } else if (!declaringCallable.equals(other.declaringCallable))
            return false;
        if (position != other.position)
            return false;
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
        throw new InvalidObjectException(PROXY_REQUIRED);
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

        private Object readResolve() {
            return callable.getParameters().get(position);
        }
    }
}
