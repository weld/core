package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.collections.WeldCollections.immutableListView;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.MethodHolder;
import org.jboss.weld.util.reflection.Formats;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

@SuppressWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR", "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedMethod<X> extends BackedAnnotatedCallable<X, Method> implements AnnotatedMethod<X>, Serializable {

    public static <X, Y extends X> AnnotatedMethod<X> of(Method method, BackedAnnotatedType<Y> declaringType, SharedObjectCache sharedObjectCache) {
        BackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedMethod<X>(method, downcastDeclaringType, sharedObjectCache);
    }

    private final Method method;

    public BackedAnnotatedMethod(Method method, BackedAnnotatedType<X> declaringType, SharedObjectCache sharedObjectCache) {
        super(method, method.getGenericReturnType(), declaringType, sharedObjectCache);
        this.method = method;
    }

    @Override
    protected List<AnnotatedParameter<X>> initParameters(Method method, SharedObjectCache sharedObjectCache) {
        final Type[] genericParameterTypes = method.getGenericParameterTypes();

        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(genericParameterTypes.length);

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type parameterType = genericParameterTypes[i];
            parameters.add(BackedAnnotatedParameter.of(parameterType, parameterAnnotations[i], i, this, sharedObjectCache));
        }
        return immutableListView(parameters);
    }

    public Method getJavaMember() {
        return method;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return method;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
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
        BackedAnnotatedMethod<?> other = (BackedAnnotatedMethod<?>) obj;
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        return true;
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
