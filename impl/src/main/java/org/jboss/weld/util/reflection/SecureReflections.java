/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.util.reflection.instantiation.InstantiatorFactory;

import static org.jboss.weld.logging.messages.UtilMessage.ANNOTATION_VALUES_INACCESSIBLE;

/**
 * @author Nicklas Karlsson
 * @author Ales Justin
 *         <p/>
 *         Utility class for SecurityManager aware reflection operations with
 *         the "weld.reflection" permission
 */
public class SecureReflections {

    /**
     * Return a named field from a class
     *
     * @param clazz     The class to operate on
     * @param fieldName The name of the field
     * @return The field
     * @throws NoSuchFieldException If the field cannot be found
     * @see java.lang.Class#getField(String))
     */
    public static Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        return new SecureReflectionAccess<Field>() {
            @Override
            protected Field work() throws Exception {
                return clazz.getField(fieldName);
            }
        }.runAsFieldAccess();
    }

    /**
     * Returns a named, declared field from a class
     *
     * @param clazz     The class to operate on
     * @param fieldName The name of the field
     * @return The field
     * @throws NoSuchFieldException If the field cannot be found
     * @see java.lang.Class#getDeclaredField(String)
     */
    public static Field getDeclaredField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        return new SecureReflectionAccess<Field>() {
            @Override
            protected Field work() throws Exception {
                return clazz.getDeclaredField(fieldName);
            }
        }.runAsFieldAccess();
    }

    /**
     * Returns all fields of a class
     *
     * @param clazz The class to operate on
     * @return The fields
     * @see java.lang.Class#getFields()
     */
    public static Field[] getFields(final Class<?> clazz) {
        return new SecureReflectionAccess<Field[]>() {
            @Override
            protected Field[] work() throws Exception {
                return clazz.getFields();
            }
        }.runAndWrap();
    }

    /**
     * Returns all declared fields of a class
     *
     * @param clazz The class to operate on
     * @return The fields
     * @see java.lang.Class#getDeclaredFields()
     */
    public static Field[] getDeclaredFields(final Class<?> clazz) {
        return new SecureReflectionAccess<Field[]>() {
            @Override
            protected Field[] work() throws Exception {
                return clazz.getDeclaredFields();
            }
        }.runAndWrap();
    }

    /**
     * Returns a named method of a class
     *
     * @param clazz          The class to operate on
     * @param methodName     The name of the method
     * @param parameterTypes The method parameter types
     * @return The method
     * @throws NoSuchMethodException If the method cannot be found
     * @see java.lang.Class#getMethod(String, Class...)
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        return new SecureReflectionAccess<Method>() {
            @Override
            protected Method work() throws Exception {
                return clazz.getMethod(methodName, parameterTypes);
            }
        }.runAsMethodAccess();
    }

    /**
     * Returns a named, declared method of a class
     *
     * @param clazz          The class to operate on
     * @param methodName     The name of the method
     * @param parameterTypes The method parameter types
     * @return The method
     * @throws NoSuchMethodException If the method cannot be found
     * @see java.lang.Class#getDeclaredMethods()
     */
    public static Method getDeclaredMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        return new SecureReflectionAccess<Method>() {
            @Override
            protected Method work() throws Exception {
                return clazz.getDeclaredMethod(methodName, parameterTypes);
            }
        }.runAsMethodAccess();
    }

    /**
     * Returns all methods of a class
     *
     * @param clazz The class to operate on
     * @return The methods
     * @see java.lang.Class#getMethods()
     */
    public static Method[] getMethods(final Class<?> clazz) {
        return new SecureReflectionAccess<Method[]>() {
            @Override
            protected Method[] work() throws Exception {
                return clazz.getMethods();
            }
        }.runAndWrap();
    }

    /**
     * Returns all declared methods of a class
     *
     * @param clazz The class to operate on
     * @return The methods
     * @see java.lang.Class#getDeclaredMethods()
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz) {
        return new SecureReflectionAccess<Method[]>() {
            @Override
            protected Method[] work() throws Exception {
                return clazz.getDeclaredMethods();
            }
        }.runAndWrap();
    }

    /**
     * Gets a constructor from a class
     *
     * @param clazz          The class to operate on
     * @param parameterTypes The constructor parameter types
     * @return The constructor
     * @throws NoSuchMethodException If the constructor cannot be found
     * @see java.lang.Class#getConstructor(Class...)
     */
    public static Constructor<?> getConstructor(final Class<?> clazz, final Class<?>... parameterTypes) throws NoSuchMethodException {
        return new SecureReflectionAccess<Constructor<?>>() {
            @Override
            protected Constructor<?> work() throws Exception {
                return clazz.getConstructor(parameterTypes);
            }
        }.runAsMethodAccess();
    }

    /**
     * Gets a declared constructor from a class
     *
     * @param clazz          The class to operate on
     * @param parameterTypes The constructor parameter types
     * @return The constructor
     * @throws NoSuchMethodException If the constructor cannot be found
     * @see java.lang.Class#getDeclaredConstructor(Class...)
     */
    public static <T> Constructor<T> getDeclaredConstructor(final Class<T> clazz, final Class<?>... parameterTypes) throws NoSuchMethodException {
        return new SecureReflectionAccess<Constructor<T>>() {
            @Override
            protected Constructor<T> work() throws Exception {
                return clazz.getDeclaredConstructor(parameterTypes);
            }
        }.runAsMethodAccess();
    }

    /**
     * Gets all constructors from a class
     *
     * @param clazz The class to operate on
     * @return The constructors
     * @see java.lang.Class#getConstructors()
     */
    public static Constructor<?>[] getConstructors(final Class<?> clazz) {
        return new SecureReflectionAccess<Constructor<?>[]>() {
            @Override
            protected Constructor<?>[] work() throws Exception {
                return clazz.getConstructors();
            }
        }.runAndWrap();
    }

    /**
     * Gets all declared constructors from a class
     *
     * @param clazz The class to operate on
     * @return The constructors
     * @see java.lang.Class#getDeclaredConstructor(Class...)
     */
    public static Constructor<?>[] getDeclaredConstructors(final Class<?> clazz) {
        return new SecureReflectionAccess<Constructor<?>[]>() {
            @Override
            protected Constructor<?>[] work() throws Exception {
                return clazz.getDeclaredConstructors();

            }
        }.runAndWrap();
    }

    /**
     * Invokes a given method with given parameters on an instance
     *
     * @param instance   The instance to invoke on
     * @param method     The method to invoke
     * @param parameters The method parameters
     * @return The return value of the method
     * @throws IllegalArgumentException  If there was an illegal argument passed
     * @throws IllegalAccessException    If there was an illegal access attempt
     * @throws InvocationTargetException If there was another error invoking the
     *                                   method
     * @see java.lang.reflect.Method#invoke(Object, Object...)
     */
    public static <T> T invoke(final Object instance, final Method method, final Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new SecureReflectionAccess<T>() {

            @Override
            protected T work() throws Exception {
                Object result = ensureAccessible(method).invoke(instance, parameters);

                //noinspection unchecked
                return (T) result;
            }

        }.runAsInvocation();
    }

    /**
     * Makes an object accessible.
     *
     * @param accessibleObject The object to manipulate
     * @return The accessible object
     */
    public static <T extends AccessibleObject> T ensureAccessible(final T accessibleObject) {
        return new SecureReflectionAccess<T>() {

            @Override
            protected T work() throws Exception {
                if (!accessibleObject.isAccessible()) {
                    accessibleObject.setAccessible(true);
                }
                return accessibleObject;
            }

        }.runAndWrap();
    }

    /**
     * Invokes a given method with given parameters on an instance
     *
     * @param instance   The instance to invoke on
     * @param methodName The name of the method to invoke
     * @param parameters The method parameters
     * @return The return value of the method
     * @throws IllegalArgumentException  If there was an illegal argument passed
     * @throws IllegalAccessException    If there was an illegal access attempt
     * @throws InvocationTargetException If there was another error invoking the
     *                                   method
     * @see java.lang.reflect.Method#invoke(Object, Object...)
     */
    public static <T> T invoke(final Object instance, final String methodName, final Object... parameters) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        return new SecureReflectionAccess<T>() {
            @Override
            protected T work() throws Exception {
                Class<?>[] parameterTypes = new Class<?>[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    parameterTypes[i] = parameters[i].getClass();
                }
                Method method = getMethod(instance.getClass(), methodName, parameterTypes);

                Object result = ensureAccessible(method).invoke(instance, parameters);

                @SuppressWarnings("unchecked")
                T t = (T) result;

                return t;

            }
        }.runAsInvocation();
    }

    /**
     * Creates a new instance of a class
     *
     * @param <T>   The type of the instance
     * @param clazz The class to construct from
     * @return The new instance
     * @throws InstantiationException If the instance could not be create
     * @throws IllegalAccessException If there was an illegal access attempt
     * @see java.lang.Class#newInstance()
     */
    public static <T> T newInstance(final Class<T> clazz) throws InstantiationException, IllegalAccessException {
        return new SecureReflectionAccess<T>() {
            @Override
            protected T work() throws Exception {
                return clazz.newInstance();
            }
        }.runAsInstantiation();
    }

    /**
     * Creates a new instance of a class using unportable methods, if available
     *
     * @param <T>   The type of the instance
     * @param clazz The class to construct from
     * @return The new instance
     * @throws InstantiationException If the instance could not be create
     * @throws IllegalAccessException If there was an illegal access attempt
     * @see java.lang.Class#newInstance()
     */
    public static <T> T newUnsafeInstance(final Class<T> clazz, final String id) throws InstantiationException, IllegalAccessException {
        return new SecureReflectionAccess<T>() {
            @Override
            protected T work() throws Exception {
                ServiceRegistry services = Container.instance(id).services();
                InstantiatorFactory factory = services.get(InstantiatorFactory.class);
                return factory.getInstantiator().instantiate(clazz);
            }
        }.runAsInstantiation();
    }

    /**
     * Looks up a method in an inheritance hierarchy
     *
     * @param instance The instance (class) to start from
     * @param method   The method to look up
     * @return The method
     * @throws NoSuchMethodException if the method could not be found
     */
    public static Method lookupMethod(Object instance, Method method) throws NoSuchMethodException {
        if (method.getDeclaringClass() == instance.getClass()) {
            return method;
        }
        return lookupMethod(instance.getClass(), method.getName(), method.getParameterTypes());
    }

    /**
     * Returns a method from the class or any class/interface in the inheritance
     * hierarchy
     *
     * @param clazz          The class to search
     * @param methodName     The method name
     * @param parameterTypes The method parameter types
     * @return The method
     * @throws NoSuchMethodException If the method could not be found
     */
    public static Method lookupMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) throws NoSuchMethodException {
        return new SecureReflectionAccess<Method>() {

            private Method lookupMethod(final Class<?> currentClass) throws NoSuchMethodException {
                for (Class<?> inspectedClass = currentClass; inspectedClass != null; inspectedClass = inspectedClass.getSuperclass()) {
                    for (Class<?> inspectedInterface : inspectedClass.getInterfaces()) {
                        try {
                            return lookupMethod(inspectedInterface);
                        } catch (NoSuchMethodException e) {
                            // Expected, nothing to see here.
                        }
                    }
                    try {
                        return getDeclaredMethod(inspectedClass, methodName, parameterTypes);
                    } catch (NoSuchMethodException nsme) {
                        // Expected, nothing to see here.
                    }
                }
                throw new NoSuchMethodException();
            }

            @Override
            protected Method work() throws Exception {
                return lookupMethod(clazz);
            }
        }.runAsMethodAccess();
    }

    /**
     * Helper class for reading the value of an annotation
     *
     * @param annotation The annotation to inspect
     * @return The array of classes
     */
    public static Class<?>[] extractValues(Annotation annotation) {
        try {
            Class<?>[] valueClasses = invoke(annotation, "value");
            return valueClasses;
        } catch (Exception e) {
            throw new DeploymentException(ANNOTATION_VALUES_INACCESSIBLE, e);
        }
    }

    /**
     * Checks if a method is found in a class
     *
     * @param clazz          The class to inspect
     * @param methodName     The name of the method
     * @param parameterTypes The parameter types of the method
     * @return true if method is present, false otherwise
     */
    public static boolean isMethodExists(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            getMethod(clazz, methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return false;
        }
        return true;
    }

}
