/**
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Reflection utilities
 *
 * @author Pete Muir
 */
public abstract class Reflections {

    private Reflections() {
    }

    public static <T> T newInstance(String className) {
        try {
            return Reflections.<T>classForName(className).newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate instance of " + className + " with no-argument constructor", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cannot instantiate instance of " + className + " with no-argument constructor", e);
        }
    }


    public static <T> Class<T> classForName(String name) {

        try {
            if (Thread.currentThread().getContextClassLoader() != null) {
                Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(name);

                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) c;

                return clazz;
            } else {
                Class<?> c = Class.forName(name);

                @SuppressWarnings("unchecked")
                Class<T> clazz = (Class<T>) c;

                return clazz;
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Cannot load class for " + name, e);
        } catch (NoClassDefFoundError e) {
            throw new IllegalArgumentException("Cannot load class for " + name, e);
        }
    }

    /**
     * Search the class hierarchy for a method with the given name and arguments.
     * Will return the nearest match, starting with the class specified and
     * searching up the hierarchy.
     *
     * @param clazz The class to search
     * @param name  The name of the method to search for
     * @param args  The arguments of the method to search for
     * @return The method found, or null if no method is found
     */
    public static Method findDeclaredMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredMethod(name, args);
            } catch (NoSuchMethodException e) {
                // No-op, continue the search
            }
        }
        return null;
    }

    /**
     * Search the class hierarchy for a field with the given name. Will return
     * the nearest match, starting with the class specified and searching up the
     * hierarchy.
     *
     * @param clazz The class to search
     * @param name  The name of the field to search for
     * @return The field found, or null if no field is found
     */
    public static Field findDeclaredField(Class<?> clazz, String name) {
        for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // No-op, we continue looking up the class hierarchy
            }
        }
        return null;
    }

    public static <T> T invokeMethod(Method method, Class<T> expectedReturnType, Object instance, Object... args) {
        try {
            return expectedReturnType.cast(method.invoke(instance, args));
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(buildInvokeMethodErrorMessage(method, instance, args), ex.getCause());
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), ex);
        } catch (NullPointerException ex) {
            NullPointerException ex2 = new NullPointerException(buildInvokeMethodErrorMessage(method, instance, args));
            ex2.initCause(ex.getCause());
            throw ex2;
        } catch (ExceptionInInitializerError e) {
            throw new RuntimeException(buildInvokeMethodErrorMessage(method, instance, args), e);
        }
    }

    private static String buildInvokeMethodErrorMessage(Method method, Object obj, Object... args) {
        StringBuilder message = new StringBuilder(String.format("Exception invoking method [%s] on object [%s], using arguments [", method.getName(), obj));
        if (args != null)
            for (int i = 0; i < args.length; i++)
                message.append((i > 0 ? "," : "") + args[i]);
        message.append("]");
        return message.toString();
    }

    public static void setFieldValue(Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(buildSetFieldValueErrorMessage(field, instance, value), e);
        } catch (NullPointerException ex) {
            NullPointerException ex2 = new NullPointerException(buildSetFieldValueErrorMessage(field, instance, value));
            ex2.initCause(ex.getCause());
            throw ex2;
        }
    }

    private static String buildSetFieldValueErrorMessage(Field field, Object obj, Object value) {
        return String.format("Exception setting [%s] field on object [%s] to value [%s]", field.getName(), obj, value);
    }

    private static String buildGetFieldValueErrorMessage(Field field, Object obj) {
        return String.format("Exception reading [%s] field from object [%s].", field.getName(), obj);
    }

    public static <T> T getFieldValue(Field field, Object instance, Class<T> expectedType) {
        field.setAccessible(true);
        try {
            return expectedType.cast(field.get(instance));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(buildGetFieldValueErrorMessage(field, instance), e);
        } catch (NullPointerException ex) {
            NullPointerException ex2 = new NullPointerException(buildGetFieldValueErrorMessage(field, instance));
            ex2.initCause(ex.getCause());
            throw ex2;
        }

    }

    public static ClassLoader getClassLoader() {
        if (Thread.currentThread().getContextClassLoader() != null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return Reflections.class.getClassLoader();
        }
    }

}
