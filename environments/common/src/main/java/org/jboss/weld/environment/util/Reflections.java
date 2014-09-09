package org.jboss.weld.environment.util;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;

public final class Reflections {

    private Reflections() {
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

    public static <T> T newInstance(ResourceLoader loader, String className, Object... parameters) {
        final Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }
        try {
            final Class<T> clazz = cast(loader.classForName(className));
            final Constructor<T> constructor = findConstructor(clazz, parameters);
            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw CommonLogger.LOG.unableToInstantiate(className, Arrays.toString(parameters), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static boolean isClassLoadable(String className, ResourceLoader resourceLoader) {
        return loadClass(className, resourceLoader) != null;
    }

    /**
     * Tries to load a class using the specified ResourceLoader. Returns null if the class is not found.
     * @param className
     * @param resourceLoader
     * @return the loaded class or null if the given class cannot be loaded
     */
    public static <T> Class<T> loadClass(String className, ResourceLoader resourceLoader) {
        try {
            return cast(resourceLoader.classForName(className));
        } catch (ResourceLoadingException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        }
    }

    /**
     *
     * @param annotations
     * @param metaAnnotationType
     * @return <code>true</code> if any of the annotations specified has the given meta annotation type specified, <code>false</code>
     *         otherwise
     */
    public static boolean hasBeanDefiningMetaAnnotationSpecified(Annotation[] annotations, Class<? extends Annotation> metaAnnotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(metaAnnotationType)) {
                return true;
            }
        }
        return false;
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
                return cast(constructor);
            }
        }
        throw CommonLogger.LOG.unableToFindConstructor(clazz, Arrays.toString(parameters));
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
}