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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.logging.ReflectionLogger;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.security.GetDeclaredMethodsAction;
import org.jboss.weld.util.Types;

/**
 * Utility class for static reflection-type operations
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class Reflections {

    public static final Type[] EMPTY_TYPES = {};
    public static final Annotation[] EMPTY_ANNOTATIONS = {};
    public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

    private Reflections() {
    }

    public static Map<Class<?>, Type> buildTypeMap(Set<Type> types) {
        Map<Class<?>, Type> map = new HashMap<Class<?>, Type>();
        for (Type type : types) {
            Class<?> clazz = getRawType(type);
            if (clazz != null) {
                map.put(clazz, type);
            }
        }
        return map;
    }

    public static boolean isCacheable(Collection<Annotation> annotations) {
        for (Annotation qualifier : annotations) {
            Class<?> clazz = qualifier.getClass();
            if (!isTopLevelOrStaticNestedClass(clazz)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCacheable(Annotation[] annotations) {
        for (Annotation qualifier : annotations) {
            Class<?> clazz = qualifier.getClass();
            if (!isTopLevelOrStaticNestedClass(clazz)) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * Gets the property name from a getter method.
     * <p/>
     * We extend JavaBean conventions, allowing the getter method to have parameters
     *
     * @param method The getter method
     * @return The name of the property. Returns null if method wasn't JavaBean
     *         getter-styled
     */
    public static String getPropertyName(Method method) {
        String methodName = method.getName();
        final String get = "get";
        final String is = "is";
        if (methodName.startsWith(get)) {
            return decapitalize(methodName.substring(get.length()));
        } else if (methodName.startsWith(is)) {
            return decapitalize(methodName.substring(is.length()));
        } else {
            return null;
        }

    }

    /**
     * Checks if class is final
     *
     * @param clazz The class to check
     * @return True if final, false otherwise
     */
    public static boolean isFinal(Class<?> clazz) {
        return Modifier.isFinal(clazz.getModifiers());
    }

    public static int getNesting(Class<?> clazz) {
        if (clazz.isMemberClass() && !isStatic(clazz)) {
            return 1 + getNesting(clazz.getDeclaringClass());
        } else {
            return 0;
        }
    }

    /**
     * Checks if member is final
     *
     * @param member The member to check
     * @return True if final, false otherwise
     */
    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    /**
     * Checks if member is private
     *
     * @param member The member to check
     * @return True if final, false otherwise
     */
    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    /**
     * Checks if type or member is final
     *
     * @param type Type or member
     * @return True if final, false otherwise
     */
    public static boolean isTypeOrAnyMethodFinal(Class<?> type) {
        return isFinal(type) || getNonPrivateNonStaticFinalMethod(type) != null;
    }

    public static Method getNonPrivateNonStaticFinalMethod(Class<?> type) {
        for (Class<?> clazz = type; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : AccessController.doPrivileged(new GetDeclaredMethodsAction(clazz))) {
                if (isFinal(method) && !isPrivate(method) && !isStatic(method) && !method.isSynthetic()) {
                    return method;
                }
            }
        }
        return null;
    }

    public static boolean isPackagePrivate(int mod) {
        return !(Modifier.isPrivate(mod) || Modifier.isProtected(mod) || Modifier.isPublic(mod));
    }

    /**
     * Checks if type is static
     *
     * @param type Type to check
     * @return True if static, false otherwise
     */
    public static boolean isStatic(Class<?> type) {
        return Modifier.isStatic(type.getModifiers());
    }

    /**
     * Checks if member is static
     *
     * @param member Member to check
     * @return True if static, false otherwise
     */
    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    /**
     * Checks if a method is abstract
     *
     * @param method the method
     * @return true if abstract
     */
    public static boolean isAbstract(Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    /**
     * Gets the actual type arguments of a class
     *
     * @param clazz The class to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Class<?> clazz) {
        Type type = Types.getCanonicalType(clazz);
        if (type instanceof ParameterizedType) {
            return ((ParameterizedType) type).getActualTypeArguments();
        } else {
            return EMPTY_TYPES;
        }
    }

    /**
     * Gets the actual type arguments of a Type
     *
     * @param type The type to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Type type) {
        Type resolvedType = Types.getCanonicalType(type);
        if (resolvedType instanceof ParameterizedType) {
            return ((ParameterizedType) resolvedType).getActualTypeArguments();
        } else {
            return EMPTY_TYPES;
        }
    }

    /**
     * Checks if raw type is array type
     *
     * @param rawType The raw type to check
     * @return True if array, false otherwise
     */
    public static boolean isArrayType(Class<?> rawType) {
        return rawType.isArray();
    }

    /**
     * Checks if type is parameterized type
     *
     * @param type The type to check
     * @return True if parameterized, false otherwise
     */
    public static boolean isParameterizedType(Class<?> type) {
        return type.getTypeParameters().length > 0;
    }

    public static boolean isParameterizedTypeWithWildcard(Class<?> type) {
        return isParameterizedType(type) && containsWildcards(type.getTypeParameters());
    }

    public static boolean containsWildcards(Type[] types) {
        for (Type type : types) {
            if (type instanceof WildcardType) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSerializable(Class<?> clazz) {
        return clazz.isPrimitive() || Serializable.class.isAssignableFrom(clazz);
    }

    public static boolean isPrimitive(Type type) {
        Class<?> rawType = getRawType(type);
        return rawType != null && rawType.isPrimitive();
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<T>) type;
        }
        if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() instanceof Class<?>) {
                return (Class<T>) ((ParameterizedType) type).getRawType();
            }
        }
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> variable = (TypeVariable<?>) type;
            Type[] bounds = variable.getBounds();
            return getBound(bounds);
        }
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            return getBound(wildcard.getUpperBounds());
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Class<?> rawType = getRawType(genericArrayType.getGenericComponentType());
            if (rawType != null) {
                return (Class<T>) Array.newInstance(rawType, 0).getClass();
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getBound(Type[] bounds) {
        if (bounds.length == 0) {
            return (Class<T>) Object.class;
        } else {
            return getRawType(bounds[0]);
        }
    }

    public static boolean isClassLoadable(String className, ResourceLoader resourceLoader) {
        return loadClass(className, resourceLoader) != null;
    }

    /**
     * Tries to load a class using the specified ResourceLoader. Returns null if the class is not found.
     *
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

    public static boolean isUnboundedWildcard(Type type) {
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            return isEmptyBoundArray(wildcard.getUpperBounds()) && isEmptyBoundArray(wildcard.getLowerBounds());
        }
        return false;
    }

    public static boolean isUnboundedTypeVariable(Type type) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            return isEmptyBoundArray(typeVariable.getBounds());
        }
        return false;
    }

    static boolean isEmptyBoundArray(Type[] bounds) {
        return bounds == null || bounds.length == 0 || (bounds.length == 1 && Object.class.equals(bounds[0]));
    }

    /**
     *
     * @param javaClass
     * @return <code>true</code> if the given class is a static nested class, <code>false</code> otherwise
     */
    public static boolean isStaticNestedClass(Class<?> javaClass) {
        if (javaClass.getEnclosingConstructor() != null || javaClass.getEnclosingMethod() != null) {
            // Local or anonymous class
            return false;
        }
        if (javaClass.getEnclosingClass() != null) {
            // Extra check for anonymous class - http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8034044
            if (javaClass.isAnonymousClass()) {
                return false;
            }
            return Reflections.isStatic(javaClass);
        }
        return false;
    }

    /**
     *
     * @param javaClass
     * @return <code>true</code> if the given class is a top-level or static nested class, <code>false</code> otherwise
     */
    public static boolean isTopLevelOrStaticNestedClass(Class<?> javaClass) {
        return javaClass.getEnclosingClass() == null || isStaticNestedClass(javaClass);
    }

    /**
     * Invokes the method on a given instance passing in given parameters. If the invocation yields
     * {@link InvocationTargetException}, the exception is unwrapped.
     *
     * It is a responsibility of the caller to make sure that the method is accessible to the caller.
     */
    public static <T> T invokeAndUnwrap(final Object instance, final Method method, final Object... parameters)
            throws Throwable {
        try {
            return cast(method.invoke(instance, parameters));
        } catch (IllegalArgumentException e) {
            throw ReflectionLogger.LOG.illegalArgumentExceptionOnReflectionInvocation(instance.getClass(), instance, method,
                    Arrays.toString(parameters), e);
        } catch (IllegalAccessException e) {
            throw new WeldException(e);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    /**
     * Triggers loading of declaring class (if any) of the given class recursively.
     * If the class cannot be loaded, the underlying {@link LinkageError} is propagated.
     *
     * @param class the given class
     * @throws LinkageError or its subclass if a declaring class cannot be loaded
     */
    public static void checkDeclaringClassLoadable(Class<?> c) {
        for (Class<?> clazz = c; clazz != null; clazz = clazz.getDeclaringClass()) {
            // noop, the loop triggers loading of the declaring classes
        }
    }

    /**
     * Searches for a declared method with a given name. If the class declares multiple methods with the given name,
     * there is no guarantee as of which methods is returned. Null is returned if the class does not declare a method
     * with the given name.
     *
     * @param clazz the given class
     * @param methodName the given method name
     * @return method method with the given name declared by the given class or null if no such method exists
     */
    public static Method findDeclaredMethodByName(Class<?> clazz, String methodName) {
        for (Method method : AccessController.doPrivileged(new GetDeclaredMethodsAction(clazz))) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    /**
     * Unwraps the given {@link InvocationTargetException}. {@link Error} and {@link Exception} are unwrapped right away,
     * {@link Throwable} is wrapped
     * within {@link WeldException}. This method never returns - it always throws the unwrapped cause instead. The return type
     * matches the throws part of
     * the signature just to simplify usage.
     *
     * @param the given exception
     * @return
     * @throws Exception unwrapped cause of {@link InvocationTargetException}
     */
    public static Exception unwrapInvocationTargetException(InvocationTargetException e) throws Exception {
        Throwable cause = e.getCause();
        if (cause instanceof Error) {
            throw (Error) cause;
        } else if (cause instanceof Exception) {
            throw (Exception) cause;
        } else {
            throw new WeldException(cause);
        }
    }

    public static Set<Class<?>> getInterfaceClosure(Class<?> clazz) {
        Set<Class<?>> result = new HashSet<>();
        for (Class<?> classToDiscover = clazz; classToDiscover != null; classToDiscover = classToDiscover.getSuperclass()) {
            addInterfaces(classToDiscover, result);
        }
        return result;
    }

    private static void addInterfaces(Class<?> clazz, Set<Class<?>> result) {
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return;
        }
        Collections.addAll(result, interfaces);
        for (Class<?> interfac : interfaces) {
            addInterfaces(interfac, result);
        }
    }

    public static boolean isDefault(Method method) {
        // Default methods are public non-abstract instance methods declared in an interface
        // Backported from JDK8
        return ((method.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC)
                && method.getDeclaringClass().isInterface();
    }

    /**
     * Copy of java.beans.Introspector#decapitalize(name) to reduce java.desktop
     * module dependency.
     *
     * Utility method to take a string and convert it to normal Java variable name
     * capitalization. This normally means converting the first character from upper
     * case to lower case, but in the (unusual) special case when there is more than
     * one character and both the first and second characters are upper case, we
     * leave it alone.
     * <p>
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as "URL".
     *
     * @param name
     *        The string to be decapitalized.
     * @return The decapitalized version of the string.
     */
    public static String decapitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0))) {
            return name;
        }
        char[] chars = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
