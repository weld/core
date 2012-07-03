package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.logging.messages.BeanMessage.PROXY_REQUIRED;
import static org.jboss.weld.util.collections.WeldCollections.immutableSet;
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
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.Container;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.collections.ArraySet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;
import org.jboss.weld.util.reflection.SecureReflections;

import com.google.common.collect.ImmutableSet;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
@SuppressWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR", "SE_BAD_FIELD_STORE", "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedType<X> extends BackedAnnotated implements SlimAnnotatedType<X>, Serializable {

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, ClassTransformer classTransformer) {
        return of(javaClass, javaClass, classTransformer);
    }

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, Type baseType, ClassTransformer classTransformer) {
        return new BackedAnnotatedType<X>(javaClass, baseType, classTransformer);
    }

    private final Class<X> javaClass;
    private final LazyValueHolder<Set<AnnotatedConstructor<X>>> constructors;
    private final LazyValueHolder<Set<AnnotatedMethod<? super X>>> methods;
    private final LazyValueHolder<Set<AnnotatedField<? super X>>> fields;
    private final ClassTransformer transformer;

    public BackedAnnotatedType(Class<X> rawType, Type baseType, ClassTransformer classTransformer) {
        super(baseType, classTransformer.getSharedObjectCache());
        this.javaClass = rawType;
        this.transformer = classTransformer;

        this.constructors = new BackedAnnotatedConstructors();
        this.fields = new BackedAnnotatedFields();
        this.methods = new BackedAnnotatedMethods();
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
        return constructors.get();
    }

    public Set<AnnotatedMethod<? super X>> getMethods() {
        return methods.get();
    }

    public Set<AnnotatedField<? super X>> getFields() {
        return fields.get();
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

    public void clear() {
        this.constructors.clear();
        this.fields.clear();
        this.methods.clear();
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

    // Lazy initialization

    // Initialize eagerly since we want to discover CNFE at bootstrap
    // After bootstrap, these holders are reset to conserve memory
    private abstract class EagerlyInitializedLazyValueHolder<T> extends LazyValueHolder<T> {
        public EagerlyInitializedLazyValueHolder() {
            this.get();
        }
    }

    private class BackedAnnotatedConstructors extends EagerlyInitializedLazyValueHolder<Set<AnnotatedConstructor<X>>> {
        @Override
        protected Set<AnnotatedConstructor<X>> computeValue() {
            Constructor<?>[] declaredConstructors = SecureReflections.getDeclaredConstructors(javaClass);
            ArraySet<AnnotatedConstructor<X>> constructors = new ArraySet<AnnotatedConstructor<X>>(declaredConstructors.length);
            for (Constructor<?> constructor : declaredConstructors) {
                Constructor<X> c = Reflections.cast(constructor);
                constructors.add(BackedAnnotatedConstructor.of(c, BackedAnnotatedType.this, transformer.getSharedObjectCache()));
            }
            return immutableSet(constructors);
        }
    }

    private class BackedAnnotatedFields extends EagerlyInitializedLazyValueHolder<Set<AnnotatedField<? super X>>> {
        @Override
        protected Set<AnnotatedField<? super X>> computeValue() {
            ArraySet<AnnotatedField<? super X>> fields = new ArraySet<AnnotatedField<? super X>>();
            Class<? super X> clazz = javaClass;
            while (clazz != Object.class && clazz != null) {
                for (Field field : SecureReflections.getDeclaredFields(clazz)) {
                    fields.add(BackedAnnotatedField.of(field, getDeclaringAnnotatedType(field, transformer), transformer.getSharedObjectCache()));
                }
                clazz = clazz.getSuperclass();
            }
            return immutableSet(fields);
        }
    }

    private class BackedAnnotatedMethods extends EagerlyInitializedLazyValueHolder<Set<AnnotatedMethod<? super X>>> {
        @Override
        protected Set<AnnotatedMethod<? super X>> computeValue() {
            ArraySet<AnnotatedMethod<? super X>> methods = new ArraySet<AnnotatedMethod<? super X>>();
            Class<? super X> clazz = javaClass;
            while (clazz != Object.class && clazz != null) {
                for (Method method : SecureReflections.getDeclaredMethods(clazz)) {
                    methods.add(BackedAnnotatedMethod.of(method, getDeclaringAnnotatedType(method, transformer), transformer.getSharedObjectCache()));
                }
                clazz = clazz.getSuperclass();
            }
            return immutableSet(methods);
        }
    }
}
