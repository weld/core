package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.collections.WeldCollections.immutableList;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.MemberTransformer;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.serialization.MethodHolder;
import org.jboss.weld.util.reflection.Formats;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedMethod<X> extends BackedAnnotatedMember<X> implements AnnotatedMethod<X>, Serializable {

    public static <X, Y extends X> AnnotatedMethod<X> of(Method method, BackedAnnotatedType<Y> declaringType, SharedObjectCache cache) {
        BackedAnnotatedType<X> downcastDeclaringType = cast(declaringType);
        return new BackedAnnotatedMethod<X>(method, downcastDeclaringType, cache);
    }

    private final Method method;
    private final List<AnnotatedParameter<X>> parameters;

    public BackedAnnotatedMethod(Method method, BackedAnnotatedType<X> declaringType, SharedObjectCache cache) {
        super(method.getGenericReturnType(), declaringType, cache);
        this.method = method;

        final Type[] genericParameterTypes = method.getGenericParameterTypes();

        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(genericParameterTypes.length);

        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type parameterType = genericParameterTypes[i];
            parameters.add(BackedAnnotatedParameter.of(parameterType, parameterAnnotations[i], i, this, cache));
        }
        this.parameters = immutableList(parameters);
    }

    public Method getJavaMember() {
        return method;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return method.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(method.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return method.isAnnotationPresent(annotationType);
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BackedAnnotatedMethod<?> other = (BackedAnnotatedMethod<?>) obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedMethod(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(method);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy implements Serializable {

        private static final long serialVersionUID = 8008578690970722095L;
        private final MethodHolder method;

        public SerializationProxy(Method method) {
            this.method = new MethodHolder(method);
        }

        private Object readResolve() {
            return Container.instance().services().get(MemberTransformer.class).loadBackedMember(method.get());
        }
    }
}
