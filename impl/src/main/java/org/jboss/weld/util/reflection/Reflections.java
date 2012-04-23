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

import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ResourceLoadingException;
import org.jboss.weld.util.Types;
import org.slf4j.cal10n.LocLogger;
import org.slf4j.ext.XLogger;

import javax.inject.Qualifier;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.jboss.weld.logging.Category.UTIL;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;

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

    public static boolean isCacheable(Set<Annotation> annotations) {
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
        if (isNonStaticInnerClass(clazz)) {
            return 1 + getNesting(clazz.getDeclaringClass());
        } else {
            return 0;
        }
    }

    public static boolean isNonStaticInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !isStatic(clazz);
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
            if (isFinal(method) && !isPrivate(method)) {
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
     * Gets the actual type arguments of a Type
     *
     * @param type The type to examine
     * @return The type arguments
     */
    public static Type[] getActualTypeArguments(Type type) {
        Type resolvedType = new HierarchyDiscovery(type).getResolvedType();
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

    public static boolean isAssignableFrom(Type type1, Set<? extends Type> types2) {
        for (Type type2 : types2) {
            if (isAssignableFrom(type1, type2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(Type requiredType, Set<? extends Type> beanTypes) {
        for (Type beanType : beanTypes) {
            if (matches(requiredType, beanType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableTo(Type type1, Type[] types2) {
        return isAssignableFrom(types2, type1);
    }

    public static boolean isAssignableFrom(Type type1, Type[] types2) {
        for (Type type2 : types2) {
            if (isAssignableFrom(type1, type2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Type type1, Type type2) {
        TypeHolder typeHolder1 = TypeHolder.wrap(type1);
        if (typeHolder1 != null && typeHolder1.isAssignableFrom(type2)) {
            return true;
        }

        return processWildcardTypesAndTypeVariables(type1, type2);
    }

    public static boolean matches(Type requiredType, Type beanType) {
        TypeHolder requiredTypeHolder = TypeHolder.wrap(requiredType);
        if (requiredTypeHolder != null && requiredTypeHolder.matches(beanType)) {
            return true;
        }

        return processWildcardTypesAndTypeVariables(requiredType, beanType);
    }

    private static boolean processWildcardTypesAndTypeVariables(Type requiredType, Type beanType) {
        if (requiredType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) requiredType;
            if (isTypeInsideBounds(beanType, wildcardType.getLowerBounds(), wildcardType.getUpperBounds())) {
                return true;
            }
        }
        if (beanType instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) beanType;
            if (isTypeInsideBounds(requiredType, wildcardType.getUpperBounds(), wildcardType.getLowerBounds())) {
                return true;
            }
        }
        if (requiredType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) requiredType;
            if (isTypeInsideBounds(beanType, EMPTY_TYPES, typeVariable.getBounds())) {
                return true;
            }
        }
        if (beanType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) beanType;
            if (isTypeInsideBounds(requiredType, typeVariable.getBounds(), EMPTY_TYPES)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTypeInsideBounds(Type type, Type[] lowerBounds, Type[] upperBounds) {
        return (lowerBounds.length == 0 || isAssignableFrom(type, lowerBounds))
            && (upperBounds.length == 0 || isAssignableTo(type, upperBounds));
    }

    /**
     * Check the assiginability of a set of <b>flattened</b> types. This
     * algorithm will check whether any of the types1 matches a type in types2
     *
     * @param types1 the types1
     * @param types2 the type2
     * @return can we assign any type from types1 to types2
     */
    public static boolean isAssignableFrom(Set<Type> types1, Set<Type> types2) {
        for (Type type : types1) {
            if (isAssignableFrom(type, types2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether whether any of the requiredTypes matches a type in beanTypes
     *
     * @param requiredTypes the requiredTypes
     * @param beanTypes     the beanTypes
     * @return can we assign any type from requiredTypes to beanTypes
     */
    public static boolean matches(Set<Type> requiredTypes, Set<Type> beanTypes) {
        for (Type requiredType : requiredTypes) {
            if (matches(requiredType, beanTypes)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the assiginability of a set of <b>flattened</b> types. This
     * algorithm will check whether any of the types1 matches a type in types2
     *
     * @param types1 the types1
     * @param type2  the type2
     * @return can we assign any type from types1 to type2
     */
    public static boolean isAssignableFrom(Set<Type> types1, Type type2) {
        for (Type type : types1) {
            if (isAssignableFrom(type, type2)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAssignableFrom(Type[] types1, Type type2) {
        for (Type type : types1) {
            if (isAssignableFrom(type, type2)) {
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
        } else if (type instanceof ParameterizedType) {
            if (((ParameterizedType) type).getRawType() instanceof Class<?>) {
                return (Class<T>) ((ParameterizedType) type).getRawType();
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Class<Object> rawComponentType = getRawType(arrayType.getGenericComponentType());
            if (rawComponentType != null) {
                return (Class<T>) Array.newInstance(rawComponentType, 0).getClass();
            }
        }
        return null;
    }

    public static boolean isClassLoadable(String className, ResourceLoader resourceLoader) {
        try {
            resourceLoader.classForName(className);
            return true;
        } catch (ResourceLoadingException e) {
            return false;
        }
    }

    /**
     * This is a helper class that holds the raw type and the actual type arguments of a Type.
     * In case of arrays, the raw type is the raw type of the array, while the actualTypeArguments are the actualTypeArguments
     * of the component type of the array.
     */
    private static class TypeHolder {

        private Class<?> rawType;
        private Type[] actualTypeArguments;

        private TypeHolder(Class<?> rawType, Type[] actualTypeArguments) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
        }

        public Class<?> getRawType() {
            return rawType;
        }

        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        private Class<?> getBoxedRawType() {
            return Types.boxedClass(getRawType());
        }

        private boolean isAssignableFrom(Type otherType) {
            TypeHolder otherTypeHolder = wrap(otherType);
            if (otherTypeHolder != null) {
                return this.isAssignableFrom(otherTypeHolder);
            }

            // TODO: this doesn't look OK!
            if (otherType instanceof TypeVariable<?>) {
                TypeVariable<?> typeVariable = (TypeVariable<?>) otherType;
                if (isTypeInsideBounds(getRawType(), EMPTY_TYPES, typeVariable.getBounds())) {
                    return true;
                }
            }
            return false;
        }

        public boolean isAssignableFrom(TypeHolder otherTypeHolder) {
            return getBoxedRawType().isAssignableFrom(otherTypeHolder.getBoxedRawType()) && areActualTypeArgumentsAssignableFrom(otherTypeHolder.getActualTypeArguments());
        }

        private boolean matches(Type otherType) {
            TypeHolder otherTypeHolder = wrap(otherType);
            return otherTypeHolder != null && this.matches(otherTypeHolder);
        }

        public boolean matches(TypeHolder otherTypeHolder) {
            return getBoxedRawType().equals(otherTypeHolder.getBoxedRawType()) && areActualTypeArgumentsAssignableFrom(otherTypeHolder.getActualTypeArguments());
        }

        private boolean areActualTypeArgumentsAssignableFrom(Type[] otherActualTypeArguments) {
            for (int i = 0; i < this.getActualTypeArguments().length; i++) {
                Type type1 = this.getActualTypeArguments()[i];
                Type type2 = otherActualTypeArguments.length > i ? otherActualTypeArguments[i] : Object.class;
                if (!Reflections.isAssignableFrom(type1, type2)) {
                    return false;
                }
            }
            return true;
        }

        private static TypeHolder wrap(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class<?>) {
                    Class<?> clazz = (Class<?>) rawType;
                    return new TypeHolder(clazz, parameterizedType.getActualTypeArguments());
                }
            } else if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>) type;
                return new TypeHolder(clazz, EMPTY_TYPES);
            } else if (type instanceof GenericArrayType) {
                GenericArrayType arrayType = (GenericArrayType) type;
                Type genericComponentType = arrayType.getGenericComponentType();
                Class<?> rawComponentType = Reflections.getRawType(genericComponentType);
                if (rawComponentType != null) {
                    Class<?> arrayClass = Array.newInstance(rawComponentType, 0).getClass();
                    return new TypeHolder(arrayClass, Reflections.getActualTypeArguments(genericComponentType));
                }
            }
            return null;
        }
    }

}