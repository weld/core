package org.jboss.weld.annotated.slim.unbacked;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.util.reflection.Formats;

public class UnbackedAnnotatedParameter<X> extends UnbackedAnnotated implements AnnotatedParameter<X>, Serializable {

    private final int position;
    private final AnnotatedCallable<X> declaringCallable;

    public UnbackedAnnotatedParameter(Type baseType, Set<Type> typeClosure, Set<Annotation> annotations, int position, AnnotatedCallable<X> declaringCallable) {
        super(baseType, typeClosure, annotations);
        this.position = position;
        this.declaringCallable = declaringCallable;
    }

    public int getPosition() {
        return position;
    }

    public AnnotatedCallable<X> getDeclaringCallable() {
        return declaringCallable;
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

        private static final long serialVersionUID = 8979519845687646272L;

        private final AnnotatedCallable<X> callable;
        private final int position;

        public SerializationProxy(UnbackedAnnotatedParameter<X> parameter) {
            this.callable = parameter.getDeclaringCallable();
            this.position = parameter.getPosition();
        }

        private Object readResolve() {
            return callable.getParameters().get(position);
        }
    }
}
