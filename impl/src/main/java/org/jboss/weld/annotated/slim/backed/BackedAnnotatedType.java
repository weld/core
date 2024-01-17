package org.jboss.weld.annotated.slim.backed;

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.InvalidObjectException;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.LazyValueHolder;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;
import org.jboss.weld.util.reflection.Reflections;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(value = { "SE_BAD_FIELD", "SE_NO_SUITABLE_CONSTRUCTOR", "SE_BAD_FIELD_STORE",
        "SE_NO_SERIALVERSIONID" }, justification = "False positive from FindBugs - serialization is handled by SerializationProxy.")
public class BackedAnnotatedType<X> extends BackedAnnotated implements SlimAnnotatedType<X>, Serializable {

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, SharedObjectCache sharedObjectCache,
            ReflectionCache reflectionCache, String contextId, String bdaId) {
        return of(javaClass, javaClass, sharedObjectCache, reflectionCache, contextId, bdaId);
    }

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, Type baseType, SharedObjectCache sharedObjectCache,
            ReflectionCache reflectionCache, String contextId, String bdaId) {
        return of(javaClass, baseType, sharedObjectCache, reflectionCache, contextId, bdaId, null);
    }

    public static <X> BackedAnnotatedType<X> of(Class<X> javaClass, Type baseType, SharedObjectCache sharedObjectCache,
            ReflectionCache reflectionCache,
            String contextId, String bdaId, String suffix) {
        return new BackedAnnotatedType<X>(javaClass, baseType, sharedObjectCache, reflectionCache, contextId, bdaId, suffix);
    }

    private final Class<X> javaClass;
    private final LazyValueHolder<Set<AnnotatedConstructor<X>>> constructors;
    private final LazyValueHolder<Set<AnnotatedMethod<? super X>>> methods;
    private final LazyValueHolder<Set<AnnotatedField<? super X>>> fields;
    private final SharedObjectCache sharedObjectCache;
    private final ReflectionCache reflectionCache;
    private final AnnotatedTypeIdentifier identifier;

    private BackedAnnotatedType(Class<X> rawType, Type baseType, SharedObjectCache sharedObjectCache,
            ReflectionCache reflectionCache, String contextId,
            String bdaId, String suffix) {
        super(baseType, sharedObjectCache);
        this.javaClass = rawType;
        this.sharedObjectCache = sharedObjectCache;
        this.reflectionCache = reflectionCache;
        this.identifier = AnnotatedTypeIdentifier.forBackedAnnotatedType(contextId, rawType, baseType, bdaId, suffix);
        this.constructors = new BackedAnnotatedConstructors();
        this.fields = new BackedAnnotatedFields();
        this.methods = new BackedAnnotatedMethods();
    }

    @Override
    protected LazyValueHolder<Set<Type>> initTypeClosure(Type baseType, SharedObjectCache cache) {
        return cache.getTypeClosureHolder(Types.getCanonicalType(baseType));
    }

    @Override
    protected AnnotatedElement getAnnotatedElement() {
        return javaClass;
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
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationType)) {
                return annotationType.cast(annotation);
            }
        }
        return null;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return getAnnotation(annotationType) != null;
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return reflectionCache.getBackedAnnotatedTypeAnnotationSet(javaClass);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof BackedAnnotatedType<?>) {
            BackedAnnotatedType<?> that = cast(obj);
            return Objects.equals(this.identifier, that.identifier);
        }
        return false;
    }

    @Override
    public String toString() {
        return Formats.formatAnnotatedType(this);
    }

    @Override
    public void clear() {
        this.constructors.clear();
        this.fields.clear();
        this.methods.clear();
    }

    // Serialization

    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<X>(getIdentifier());
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw BeanLogger.LOG.serializationProxyRequired();
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
            Constructor<?>[] declaredConstructors = SecurityActions.getDeclaredConstructors(javaClass);
            ImmutableSet.Builder<AnnotatedConstructor<X>> constructors = ImmutableSet.builder();
            for (Constructor<?> constructor : declaredConstructors) {
                Constructor<X> c = Reflections.cast(constructor);
                constructors.add(BackedAnnotatedConstructor.of(c, BackedAnnotatedType.this, sharedObjectCache));
            }
            return constructors.build();
        }
    }

    private class BackedAnnotatedFields extends EagerlyInitializedLazyValueHolder<Set<AnnotatedField<? super X>>> {
        @Override
        protected Set<AnnotatedField<? super X>> computeValue() {
            ImmutableSet.Builder<AnnotatedField<? super X>> fields = ImmutableSet.builder();
            Class<? super X> clazz = javaClass;
            while (clazz != Object.class && clazz != null) {
                for (Field field : SecurityActions.getDeclaredFields(clazz)) {
                    fields.add(BackedAnnotatedField.of(field, getDeclaringAnnotatedType(field), sharedObjectCache));
                }
                clazz = clazz.getSuperclass();
            }
            return fields.build();
        }
    }

    private class BackedAnnotatedMethods extends EagerlyInitializedLazyValueHolder<Set<AnnotatedMethod<? super X>>> {
        @Override
        protected Set<AnnotatedMethod<? super X>> computeValue() {
            ImmutableSet.Builder<AnnotatedMethod<? super X>> methods = ImmutableSet.builder();
            Class<? super X> clazz = javaClass;
            while (clazz != Object.class && clazz != null) {
                for (Method method : SecurityActions.getDeclaredMethods(clazz)) {
                    methods.add(BackedAnnotatedMethod.of(method, getDeclaringAnnotatedType(method), sharedObjectCache));
                }
                clazz = clazz.getSuperclass();
            }
            // Also add default methods
            for (Class<?> interfaceClazz : Reflections.getInterfaceClosure(javaClass)) {
                for (Method method : SecurityActions.getDeclaredMethods(interfaceClazz)) {
                    if (Reflections.isDefault(method)) {
                        methods.add(BackedAnnotatedMethod.of(method, getDeclaringAnnotatedType(method), sharedObjectCache));
                    }
                }
            }
            return methods.build();
        }
    }

    private BackedAnnotatedType<? super X> getDeclaringAnnotatedType(Member member) {
        Class<? super X> declaringClass = cast(member.getDeclaringClass());
        if (declaringClass.equals(getJavaClass())) {
            return this;
        } else {
            return BackedAnnotatedType.of(declaringClass, sharedObjectCache, reflectionCache, identifier.getSuffix(),
                    identifier.getBdaId());
        }
    }

    public ReflectionCache getReflectionCache() {
        return reflectionCache;
    }

    @Override
    public AnnotatedTypeIdentifier getIdentifier() {
        return identifier;
    }
}
