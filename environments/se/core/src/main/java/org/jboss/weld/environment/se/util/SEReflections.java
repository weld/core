package org.jboss.weld.environment.se.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.util.reflection.Reflections;

public class SEReflections {

    private SEReflections() {
    }

    public static boolean containsAnnotation(Class<?> javaClass, Class<? extends Annotation> requiredAnnotation) {
        for (Class<?> clazz = javaClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            // class level annotations
            if (clazz == javaClass || requiredAnnotation.isAnnotationPresent(Inherited.class)) {
                if (containsAnnotations(clazz.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
            }
            // fields
            for (Field field : clazz.getDeclaredFields()) {
                if (containsAnnotations(field.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
            }
            // constructors
            for (Constructor<?> constructor : clazz.getConstructors()) {
                if (containsAnnotations(constructor.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : constructor.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
            // methods
            for (Method method : clazz.getDeclaredMethods()) {
                if (containsAnnotations(method.getAnnotations(), requiredAnnotation)) {
                    return true;
                }
                for (Annotation[] parameterAnnotations : method.getParameterAnnotations()) {
                    if (containsAnnotations(parameterAnnotations, requiredAnnotation)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean containsAnnotations(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation) {
        return containsAnnotation(annotations, requiredAnnotation, true);
    }

    private static boolean containsAnnotation(Annotation[] annotations, Class<? extends Annotation> requiredAnnotation, boolean checkMetaAnnotations) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (requiredAnnotation.equals(annotationType)) {
                return true;
            }
            if (checkMetaAnnotations && containsAnnotation(annotationType.getAnnotations(), requiredAnnotation, false)) {
                return true;
            }
        }
        return false;
    }

    public static <T> T newInstance(ResourceLoader loader, String className, Object... parameters) {
        final Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        try {
            final Class<T> clazz = Reflections.cast(loader.classForName(className));
            final Constructor<T> constructor = findConstructor(clazz, parameters);
            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to instantiate " + className + " using parameters: " + Arrays.toString(parameters), e);
        }
    }

    /**
     * Does constructor lookup respecting parameter covariance.
     */
    private static <T> Constructor<T> findConstructor(Class<T> clazz, Object... parameters) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            boolean match = true;
            for (int i = 0; i < parameters.length; i++) {
                if (!constructor.getParameterTypes()[i].isAssignableFrom(parameters[i].getClass())) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return Reflections.cast(constructor);
            }
        }
        throw new IllegalStateException("Unable to find constructor for of " + clazz + " accepting parameters: " + Arrays.toString(parameters));
    }
}