package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.collections.WeldCollections.immutableList;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;

import org.jboss.weld.Container;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.MemberTransformer;
import org.jboss.weld.serialization.ConstructorHolder;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedConstructor<X> extends BackedAnnotatedMember<X> implements AnnotatedConstructor<X>, Serializable {

    public static <X> AnnotatedConstructor<X> of(Constructor<X> constructor, BackedAnnotatedType<X> declaringType) {
        return new BackedAnnotatedConstructor<X>(constructor, declaringType);
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public BackedAnnotatedConstructor(Constructor<X> constructor, BackedAnnotatedType<X> declaringType) {
        super(constructor.getDeclaringClass(), declaringType);
        this.constructor = constructor;

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Type[] genericParameterTypes = constructor.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

        if (parameterTypes.length == genericParameterTypes.length && genericParameterTypes.length == parameterAnnotations.length) {
            List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(parameterTypes.length);
            int nesting = Reflections.getNesting(declaringType.getJavaClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                int gi = i - nesting;
                Class<?> clazz = parameterTypes[i];

                Type parameterType;
                int position;
                if (constructor.getGenericParameterTypes().length > gi && gi >= 0) {
                    parameterType = constructor.getGenericParameterTypes()[gi];
                    position = gi;
                } else {
                    parameterType = clazz;
                    position = i;
                }
                parameters.add(new BackedAnnotatedParameter<X>(parameterType, parameterAnnotations[position], position, this));
            }
            this.parameters = immutableList(parameters);
        } else {
            /*
             * We are seeing either http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6520205 or
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5087240 or both.
             *
             * It is difficult to detect and adapt to these bugs properly. Therefore, we pretend to see a no-args constructor.
             * Although misleading, it is quite safe to do that since non-static inner classes are not CDI-managed beans anyway
             * and CDI constructor injection into Enums is not supported.
             */
            this.parameters = Collections.emptyList();
        }
    }

    public Constructor<X> getJavaMember() {
        return constructor;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return constructor.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(constructor.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return constructor.isAnnotationPresent(annotationType);
    }

    public List<AnnotatedParameter<X>> getParameters() {
        return parameters;
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BackedAnnotatedConstructor<?> other = (BackedAnnotatedConstructor<?>) obj;
        if (constructor == null) {
            if (other.constructor != null)
                return false;
        } else if (!constructor.equals(other.constructor))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedConstructor(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(constructor);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<X> implements Serializable {

        private static final long serialVersionUID = -2726172060851333254L;
        private final ConstructorHolder<X> constructor;

        public SerializationProxy(Constructor<X> constructor) {
            this.constructor = new ConstructorHolder<X>(constructor);
        }

        private Object readResolve() {
            return Container.instance().services().get(MemberTransformer.class).loadBackedMember(constructor.get());
        }
    }
}
