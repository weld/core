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

import static org.jboss.weld.logging.Category.UTIL;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Qualifier;

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Types;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

/**
 * Utility class for static reflection-type operations
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class Reflections {

    static final LocLogger log = loggerFactory().getLogger(UTIL);
    static final XLogger xLog = loggerFactory().getXLogger(UTIL);

    public static final Type[] EMPTY_TYPES = {};
    public static final Annotation[] EMPTY_ANNOTATIONS = {};
    public static final Class<?>[] EMPTY_CLASSES = new Class<?>[0];

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
            if (clazz.isAnonymousClass() || (clazz.isMemberClass() && isStatic(clazz))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCacheable(Annotation[] annotations) {
        for (Annotation qualifier : annotations) {
            Class<?> clazz = qualifier.getClass();
            if (clazz.isAnonymousClass() || (clazz.isMemberClass() && isStatic(clazz))) {
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
        if (methodName.matches("^(get).*")) {
            return Introspector.decapitalize(methodName.substring(3));
        } else if (methodName.matches("^(is).*")) {
            return Introspector.decapitalize(methodName.substring(2));
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
        return getNonPrivateFinalMethodOrType(type) != null;
    }

    public static Object getNonPrivateFinalMethodOrType(Class<?> type) {
        if (isFinal(type)) {
            return type;
        }
        for (Method method : type.getDeclaredMethods()) {
            if (isFinal(method) && !isPrivate(method) && !isStatic(method)) {
                return method;
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

    /**
     * Gets the actual type arguments of a class
     *
     * @param clazz The class to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Class<?> clazz) {
        Type type = Types.resolveType(clazz);
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
        Type resolvedType = Types.resolveType(type);
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

    public static boolean isParamerterizedTypeWithWildcard(Class<?> type) {
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

    /**
     * Checks the bindingType to make sure the annotation was declared properly
     * as a binding type (annotated with @BindingType) and that it has a runtime
     * retention policy.
     *
     * @param binding The binding type to check
     * @return true only if the annotation is really a binding type
     */
    @Deprecated
    // TODO Replace usage of this with metadatacache
    public static boolean isBindings(Annotation binding) {
        boolean isBindingAnnotation = false;
        if (binding.annotationType().isAnnotationPresent(Qualifier.class) && binding.annotationType().isAnnotationPresent(Retention.class) && binding.annotationType().getAnnotation(Retention.class).value().equals(RetentionPolicy.RUNTIME)) {
            isBindingAnnotation = true;
        }
        return isBindingAnnotation;
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
        try {
            resourceLoader.classForName(className);
            return true;
        } catch (ResourceLoadingException e) {
            return false;
        }
    }

    public static boolean isEnum(Class<?> clazz) {
        return clazz.isEnum() || (clazz.getSuperclass() != null && clazz.getSuperclass().isEnum());
    }

    public static boolean isArrayOfUnboundedTypeVariablesOrObjects(Type[] types) {
        for (Type type : types) {
            if (Object.class.equals(type)) {
                continue;
            }
            if (type instanceof TypeVariable<?>) {
                Type[] bounds = ((TypeVariable<?>) type).getBounds();
                if (isEmptyBoundArray(bounds)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    public static boolean isUnboundedWildcard(Type type) {
        if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            return isEmptyBoundArray(wildcard.getUpperBounds()) && isEmptyBoundArray(wildcard.getLowerBounds());
        }
        return false;
    }

    private static boolean isEmptyBoundArray(Type[] bounds) {
        return bounds == null || bounds.length == 0 || (bounds.length == 1 && Object.class.equals(bounds[0]));
    }
}
