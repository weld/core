package org.jboss.weld.annotated.backed;

import static java.util.Collections.unmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;

import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.reflection.Reflections;

import com.google.common.collect.ImmutableSet;

public class BackedAnnotatedConstructor<X> extends BackedAnnotatedMember<X> implements AnnotatedConstructor<X> {

    public static <X> AnnotatedConstructor<X> of(Constructor<X> constructor, AnnotatedType<X> declaringType) {
        return new BackedAnnotatedConstructor<X>(constructor, declaringType);
    }

    public static <X> AnnotatedConstructor<X> of(AnnotatedConstructor<X> originalConstructor, AnnotatedType<X> declaringType) {
        return new BackedAnnotatedConstructor<X>(originalConstructor.getBaseType(), declaringType, originalConstructor.getParameters(), originalConstructor.getJavaMember());
    }

    private final Constructor<X> constructor;
    private final List<AnnotatedParameter<X>> parameters;

    public BackedAnnotatedConstructor(Type baseType, AnnotatedType<X> declaringType, List<AnnotatedParameter<X>> originalParameters, Constructor<X> constructor) {
        super(baseType, declaringType);
        this.constructor = constructor;
        List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(originalParameters.size());
        for (AnnotatedParameter<X> originalParameter : originalParameters) {
            parameters.add(new BackedAnnotatedParameter<X>(originalParameter.getBaseType(), originalParameter.getAnnotations(), originalParameter.getPosition(), this));
        }
        this.parameters = unmodifiableList(parameters);
    }

    public BackedAnnotatedConstructor(Constructor<X> constructor, AnnotatedType<X> declaringType) {
        super(constructor.getDeclaringClass(), declaringType);
        this.constructor = constructor;

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Type[] genericParameterTypes = constructor.getGenericParameterTypes();

        if (parameterTypes.length == genericParameterTypes.length) {
            final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            List<AnnotatedParameter<X>> parameters = new ArrayList<AnnotatedParameter<X>>(parameterTypes.length);
            int nesting = Reflections.getNesting(declaringType.getJavaClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                int gi = i - nesting;
                Class<?> clazz = parameterTypes[i];

                Type parameterType;
                if (constructor.getGenericParameterTypes().length > gi && gi >= 0) {
                    parameterType = constructor.getGenericParameterTypes()[gi];
                } else {
                    parameterType = clazz;
                }

                Annotation[] annotations;
                if (gi >= 0 && parameterAnnotations[gi].length > 0) {
                    annotations = parameterAnnotations[gi];
                } else {
                    annotations = Arrays2.EMPTY_ANNOTATION_ARRAY;
                }
                parameters.add(new BackedAnnotatedParameter<X>(parameterType, Arrays2.asSet(annotations), i, this)); // TODO
            }
            this.parameters = Collections.unmodifiableList(parameters);
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

}
