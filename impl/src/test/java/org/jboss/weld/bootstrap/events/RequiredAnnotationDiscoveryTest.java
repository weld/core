/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.bootstrap.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.jboss.weld.annotated.slim.AnnotatedTypeIdentifier;
import org.jboss.weld.annotated.slim.backed.BackedAnnotatedType;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.resources.ReflectionCache;
import org.jboss.weld.resources.ReflectionCacheFactory;
import org.jboss.weld.resources.SharedObjectCache;
import org.jboss.weld.util.reflection.Reflections;
import org.junit.Test;

/**
 * Checks that finding the matching annotations of a class once gives the same answers as the original search,
 * which walked the class again for every required annotation.
 */
public class RequiredAnnotationDiscoveryTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnClass {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnField {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnMethod {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnParameter {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnConstructor {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnConstructorParameter {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnDefaultMethod {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OnAbstractMethod {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Unused {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Marker {
    }

    @Marker
    @Retention(RetentionPolicy.RUNTIME)
    @interface Composed {
    }

    @Composed
    @Retention(RetentionPolicy.RUNTIME)
    @interface ComposedTwice {
    }

    @OnClass
    static class OnClassBean {
    }

    @Composed
    static class ComposedBean {
    }

    @ComposedTwice
    static class ComposedTwiceBean {
    }

    static class FieldBean {
        @OnField
        String field;
    }

    static class MethodBean {
        @OnMethod
        void method() {
        }
    }

    static class ParameterBean {
        void method(@OnParameter String parameter) {
        }
    }

    static class ConstructorBean {
        @OnConstructor
        ConstructorBean() {
        }
    }

    static class ConstructorParameterBean {
        ConstructorParameterBean(@OnConstructorParameter String parameter) {
        }
    }

    static class InheritingBean extends MethodBean {
    }

    @Composed
    static class ComposedMethodBase {
        @Composed
        void method() {
        }
    }

    static class InheritingComposedBean extends ComposedMethodBase {
    }

    interface WithDefaultMethod {
        @OnDefaultMethod
        default void defaultMethod() {
        }

        @OnAbstractMethod
        void abstractMethod();
    }

    static abstract class DefaultMethodBean implements WithDefaultMethod {
    }

    static class PlainBean {
        void method(String parameter) {
        }
    }

    private static final List<Class<?>> BEANS = List.of(OnClassBean.class, ComposedBean.class, ComposedTwiceBean.class,
            FieldBean.class, MethodBean.class, ParameterBean.class, ConstructorBean.class,
            ConstructorParameterBean.class, InheritingBean.class, InheritingComposedBean.class,
            DefaultMethodBean.class, PlainBean.class);

    private static final List<Class<? extends Annotation>> REQUIRED = List.of(OnClass.class, OnField.class,
            OnMethod.class, OnParameter.class, OnConstructor.class, OnConstructorParameter.class,
            OnDefaultMethod.class, OnAbstractMethod.class, Unused.class, Marker.class, Composed.class,
            ComposedTwice.class);

    private final ReflectionCache cache = ReflectionCacheFactory.newInstance(new TypeStore());
    private final RequiredAnnotationDiscovery discovery = new RequiredAnnotationDiscovery(cache);

    private BackedAnnotatedType<?> annotatedType(Class<?> javaClass) {
        return BackedAnnotatedType.of(javaClass, new SharedObjectCache(), cache, "test",
                AnnotatedTypeIdentifier.NULL_BDA_ID);
    }

    @Test
    public void findsAnnotationsWhereverTheSpecificationLooks() {
        assertTrue(discovery.containsAnnotation(annotatedType(OnClassBean.class), OnClass.class));
        assertTrue(discovery.containsAnnotation(annotatedType(FieldBean.class), OnField.class));
        assertTrue(discovery.containsAnnotation(annotatedType(MethodBean.class), OnMethod.class));
        assertTrue(discovery.containsAnnotation(annotatedType(ParameterBean.class), OnParameter.class));
        assertTrue(discovery.containsAnnotation(annotatedType(ConstructorBean.class), OnConstructor.class));
        assertTrue(discovery.containsAnnotation(annotatedType(ConstructorParameterBean.class),
                OnConstructorParameter.class));
        assertTrue(discovery.containsAnnotation(annotatedType(InheritingBean.class), OnMethod.class));
        assertTrue(discovery.containsAnnotation(annotatedType(DefaultMethodBean.class), OnDefaultMethod.class));
    }

    @Test
    public void ignoresWhatTheSpecificationDoesNotCover() {
        // only default methods of interfaces count
        assertFalse(discovery.containsAnnotation(annotatedType(DefaultMethodBean.class), OnAbstractMethod.class));
        // a meta-annotation counts one level deep only
        assertTrue(discovery.containsAnnotation(annotatedType(ComposedBean.class), Marker.class));
        assertFalse(discovery.containsAnnotation(annotatedType(ComposedTwiceBean.class), Marker.class));
        assertFalse(discovery.containsAnnotation(annotatedType(PlainBean.class), Unused.class));
    }

    @Test
    public void givesTheSameAnswersAsTheOriginalSearch() {
        for (Class<?> bean : BEANS) {
            BackedAnnotatedType<?> annotatedType = annotatedType(bean);
            for (Class<? extends Annotation> required : REQUIRED) {
                assertEquals(bean.getSimpleName() + " " + required.getSimpleName(),
                        originalContainsAnnotation(annotatedType, required),
                        discovery.containsAnnotation(annotatedType, required));
            }
        }
    }

    @Test
    public void givesTheSameAnswersAfterCleanup() {
        BackedAnnotatedType<?> annotatedType = annotatedType(MethodBean.class);
        assertTrue(discovery.containsAnnotation(annotatedType, OnMethod.class));
        discovery.cleanupAfterBoot();
        assertTrue(discovery.containsAnnotation(annotatedType, OnMethod.class));
        assertFalse(discovery.containsAnnotation(annotatedType, Unused.class));
    }

    // The search as it was before the matching annotations were collected once per class.

    private boolean originalContainsAnnotation(BackedAnnotatedType<?> annotatedType,
            Class<? extends Annotation> requiredAnnotation) {
        if (containsAnnotation(annotatedType.getAnnotations(), requiredAnnotation, true)) {
            return true;
        }
        for (Class<?> clazz = annotatedType.getJavaClass(); clazz != null
                && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotation(cache.getAnnotations(field), requiredAnnotation, true)) {
                    return true;
                }
            }
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                if (containsAnnotation(cache.getAnnotations(constructor), requiredAnnotation, true)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : constructor.getParameterAnnotations()) {
                    if (containsAnnotation(java.util.Arrays.asList(parameterAnnotations), requiredAnnotation, true)) {
                        return true;
                    }
                }
            }
            for (Method method : clazz.getDeclaredMethods()) {
                if (containsAnnotation(cache.getAnnotations(method), requiredAnnotation, true)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                    if (containsAnnotation(java.util.Arrays.asList(parameterAnnotations), requiredAnnotation, true)) {
                        return true;
                    }
                }
            }
        }
        for (Class<?> interfaceClazz : Reflections.getInterfaceClosure(annotatedType.getJavaClass())) {
            for (Method method : interfaceClazz.getDeclaredMethods()) {
                if (Reflections.isDefault(method)) {
                    if (containsAnnotation(cache.getAnnotations(method), requiredAnnotation, true)) {
                        return true;
                    }
                    for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                        if (containsAnnotation(java.util.Arrays.asList(parameterAnnotations), requiredAnnotation,
                                true)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean containsAnnotation(Iterable<? extends Annotation> annotations,
            Class<? extends Annotation> requiredAnnotation, boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (requiredAnnotation.equals(annotationType)) {
                return true;
            }
            if (checkMetaAnnotations
                    && containsAnnotation(cache.getAnnotations(annotationType), requiredAnnotation, false)) {
                return true;
            }
        }
        return false;
    }
}
