package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedType<X> extends BackedAnnotated implements SlimAnnotatedType<X>, Serializable {

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, ClassTransformer classTransformer) {
        return of(javaClass, javaClass, classTransformer);
    }

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, Type baseType, ClassTransformer classTransformer) {
        return new BackedAnnotatedType<X>(javaClass, baseType, classTransformer);
    }

    private final Class<X> javaClass;
    private final Set<AnnotatedConstructor<X>> constructors;
    private final Set<AnnotatedMethod<? super X>> methods;
    private final Set<AnnotatedField<? super X>> fields;

    public BackedAnnotatedType(Class<X> rawType, Type baseType, ClassTransformer classTransformer) {
        super(baseType);
        this.javaClass = rawType;
        // TODO this all should be initialized lazily so that we can serialize the AnnotatedType

        Constructor<?>[] declaredConstructors = SecureReflections.getDeclaredConstructors(javaClass);
        ArraySet<AnnotatedConstructor<X>> constructors = new ArraySet<AnnotatedConstructor<X>>(declaredConstructors.length);
        ArraySet<AnnotatedMethod<? super X>> methods = new ArraySet<AnnotatedMethod<? super X>>();
        ArraySet<AnnotatedField<? super X>> fields = new ArraySet<AnnotatedField<? super X>>();

        for (Constructor<?> constructor : declaredConstructors) {
            Constructor<X> c = Reflections.cast(constructor);
            constructors.add(BackedAnnotatedConstructor.of(c, this));
        }
        Class<? super X> clazz = javaClass;
        while (clazz != Object.class && clazz != null) {
            for (Method method : SecureReflections.getDeclaredMethods(clazz)) {
                methods.add(BackedAnnotatedMethod.of(method, getDeclaringAnnotatedType(method, classTransformer)));
            }
            for (Field field : SecureReflections.getDeclaredFields(clazz)) {
                fields.add(BackedAnnotatedField.of(field, getDeclaringAnnotatedType(field, classTransformer)));
            }
            clazz = clazz.getSuperclass();
        }
        this.constructors = Collections.unmodifiableSet(constructors.trimToSize());
        this.methods = Collections.unmodifiableSet(methods.trimToSize());
        this.fields = Collections.unmodifiableSet(fields.trimToSize());
    }

    private <T> BackedAnnotatedType<T> getDeclaringAnnotatedType(Member member, ClassTransformer transformer) {
        if (member.getDeclaringClass().equals(getJavaClass())) {
            return cast(this);
        } else {
            return transformer.getAnnotatedType(Reflections.<Class<T>> cast(member.getDeclaringClass()));
        }
    }

    public Class<X> getJavaClass() {
        return javaClass;
    }

    public Set<AnnotatedConstructor<X>> getConstructors() {
        return constructors;
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return methods;
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return fields;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return javaClass.getAnnotation(annotationType);
    }

    public Set<Annotation> getAnnotations() {
        return ImmutableSet.copyOf(javaClass.getAnnotations());
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return javaClass.isAnnotationPresent(annotationType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getBaseType() == null) ? 0 : getBaseType().hashCode());
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
        BackedAnnotatedType<?> other = (BackedAnnotatedType<?>) obj;
        if (getBaseType() == null) {
            if (other.getBaseType() != null)
                return false;
        } else if (!getBaseType().equals(other.getBaseType()))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedType(this);
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(javaClass);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException(PROXY_REQUIRED);
    }

    private static class SerializationProxy<X> implements Serializable {

        private static final long serialVersionUID = 6346909556206514705L;
        private final Class<X> javaClass;

        public SerializationProxy(Class<X> javaClass) {
            this.javaClass = javaClass;
        }

        private Object readResolve() {
            return Container.instance().services().get(ClassTransformer.class).getAnnotatedType(javaClass);
        }
    }
}
