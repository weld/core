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
package org.jboss.weld.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.logging.UtilLogger;
import org.jboss.weld.logging.ValidatorLogger;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.collections.ImmutableMap;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Utilities for working with proxies.
 *
 * @author Nicklas Karlsson
 * @author Pete Muir
 * @author Tomaz Cerar
 * @author Ales Justin
 */
@SuppressWarnings({ "ThrowableResultOfMethodCallIgnored", "NullableProblems" })
public class Proxies {

    public static class TypeInfo {

        private static final String DEFAULT_PACKAGE = "";

        private final List<Class<?>> interfaces;
        private final List<Class<?>> classes;
        private final Map<String, String> classToPackageMap;

        private TypeInfo(Set<? extends Type> types) {
            List<Class<?>> foundInterfaces = new ArrayList<>();
            List<Class<?>> foundClasses = new ArrayList<>();
            Map<String, String> classToPackage = new HashMap<>();

            types.stream().forEach(type -> add(type, foundInterfaces, foundClasses, classToPackage));

            // sort both collections and create immutable collections
            Collections.sort(foundClasses, Comparator.comparing(Class::getName));
            Collections.sort(foundInterfaces, new CustomClassComparator());
            this.interfaces = ImmutableList.copyOf(foundInterfaces);
            this.classes = ImmutableList.copyOf(foundClasses);
            this.classToPackageMap = ImmutableMap.copyOf(classToPackage);
        }

        // only invoked during object construction, arrays are then immutable
        private TypeInfo add(Type type, List<Class<?>> foundInterfaces, List<Class<?>> foundClasses,
                Map<String, String> classToPackageMap) {
            if (type instanceof Class<?>) {
                Class<?> clazz = (Class<?>) type;
                classToPackageMap.put(clazz.getName(),
                        clazz.getPackage() == null ? DEFAULT_PACKAGE : clazz.getPackage().getName());
                if (clazz.isInterface()) {
                    foundInterfaces.add(clazz);
                } else {
                    foundClasses.add(clazz);
                }
            } else if (type instanceof ParameterizedType) {
                add(((ParameterizedType) type).getRawType(), foundInterfaces, foundClasses, classToPackageMap);
            } else {
                throw UtilLogger.LOG.cannotProxyNonClassType(type);
            }
            return this;
        }

        public Class<?> getSuperClass() {
            if (classes.isEmpty()) {
                return Object.class;
            }
            Iterator<Class<?>> it = classes.iterator();
            Class<?> superclass = it.next();
            while (it.hasNext()) {
                Class<?> clazz = it.next();
                if (superclass.isAssignableFrom(clazz)) {
                    superclass = clazz;
                }
            }
            return superclass;
        }

        public Class<?> getSuperInterface() {
            if (interfaces.isEmpty()) {
                return null;
            }
            Iterator<Class<?>> it = interfaces.iterator();
            Class<?> superclass = it.next();
            while (it.hasNext()) {
                Class<?> clazz = it.next();
                if (superclass.isAssignableFrom(clazz)) {
                    superclass = clazz;
                }
            }
            return superclass;
        }

        public List<Class<?>> getClasses() {
            return classes;
        }

        public List<Class<?>> getInterfaces() {
            return interfaces;
        }

        public String getPackageNameForClass(Class<?> clazz) {
            return classToPackageMap.get(clazz.getName());
        }

        public static TypeInfo of(Set<? extends Type> types) {
            return new TypeInfo(types);
        }

    }

    private Proxies() {
    }

    /**
     * Indicates if a class is proxyable
     *
     * @param type The class to test
     * @return True if proxyable, false otherwise
     */
    public static boolean isTypeProxyable(Type type, ServiceRegistry services) {
        return getUnproxyableTypeException(type, services) == null;
    }

    public static UnproxyableResolutionException getUnproxyableTypeException(Type type, ServiceRegistry services) {
        return getUnproxyableTypeException(type, null, services, false);
    }

    /**
     * Indicates if a set of types are all proxyable
     *
     * @param declaringBean with types to test
     * @return True if proxyable, false otherwise
     */
    public static boolean isTypesProxyable(Bean<?> declaringBean, ServiceRegistry services) {
        return getUnproxyableTypesException(declaringBean, services) == null;
    }

    /**
     * Indicates if a set of types are all proxyable
     *
     * @param types The types to test
     * @return True if proxyable, false otherwise
     */
    public static boolean isTypesProxyable(Iterable<? extends Type> types, ServiceRegistry services) {
        return getUnproxyableTypesException(types, services) == null;
    }

    public static UnproxyableResolutionException getUnproxyableTypesException(Bean<?> declaringBean, ServiceRegistry services) {
        if (declaringBean == null) {
            throw new java.lang.IllegalArgumentException("Null declaring bean!");
        }

        return getUnproxyableTypesExceptionInt(declaringBean.getTypes(), declaringBean, services);
    }

    public static UnproxyableResolutionException getUnproxyableTypesException(Iterable<? extends Type> types,
            ServiceRegistry services) {
        return getUnproxyableTypesExceptionInt(types, null, services);
    }

    public static UnproxyableResolutionException getUnproxyableTypeException(Type type, Bean<?> declaringBean,
            ServiceRegistry services, boolean ignoreFinalMethods) {
        if (type instanceof Class<?> || type instanceof ParameterizedType || type instanceof GenericArrayType) {
            return getUnproxyableClassException(Reflections.getRawType(type), declaringBean, services, ignoreFinalMethods);
        }
        return ValidatorLogger.LOG.notProxyableUnknown(type, getDeclaringBeanInfo(declaringBean));
    }

    // --- private

    private static UnproxyableResolutionException getUnproxyableTypesExceptionInt(Iterable<? extends Type> types,
            Bean<?> declaringBean, ServiceRegistry services) {
        for (Type apiType : types) {
            if (Object.class.equals(apiType)) {
                continue;
            }
            UnproxyableResolutionException e = getUnproxyableTypeException(apiType, declaringBean, services, false);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    private static UnproxyableResolutionException getUnproxyableClassException(Class<?> clazz, Bean<?> declaringBean,
            ServiceRegistry services,
            boolean ignoreFinalMethods) {
        if (clazz.isInterface()) {
            return null;
        }

        Constructor<?> constructor = null;
        try {
            constructor = SecurityActions.getDeclaredConstructor(clazz);
        } catch (Exception ignored) {
        }

        if (clazz.isPrimitive()) {
            return ValidatorLogger.LOG.notProxyablePrimitive(clazz, getDeclaringBeanInfo(declaringBean));
        } else if (Reflections.isArrayType(clazz)) {
            return ValidatorLogger.LOG.notProxyableArrayType(clazz, getDeclaringBeanInfo(declaringBean));
        } else if (Reflections.isFinal(clazz)) {
            return ValidatorLogger.LOG.notProxyableFinalType(clazz, getDeclaringBeanInfo(declaringBean));
        } else {
            Method finalMethod = Reflections.getNonPrivateNonStaticFinalMethod(clazz);
            if (finalMethod != null) {
                if (ignoreFinalMethods || Beans.shouldIgnoreFinalMethods(declaringBean)
                        || services.get(WeldConfiguration.class).isFinalMethodIgnored(clazz.getName())) {
                    ValidatorLogger.LOG.notProxyableFinalMethodIgnored(finalMethod, getDeclaringBeanInfo(declaringBean));
                } else {
                    return ValidatorLogger.LOG.notProxyableFinalMethod(clazz, finalMethod, getDeclaringBeanInfo(declaringBean));
                }
            }
        }

        UnproxyableResolutionException exception = services.get(ProxyInstantiator.class).validateNoargConstructor(constructor,
                clazz, declaringBean);
        if (exception != null) {
            return exception;
        }

        return null;
    }

    public static Object getDeclaringBeanInfo(Bean<?> bean) {
        return (bean != null) ? bean : "<unknownjakarta.enterprise.inject.spi.Bean instance>";
    }

    /**
     *
     * @param interfaces
     * @return the sorted set of interfaces
     */
    public static LinkedHashSet<Class<?>> sortInterfacesHierarchy(Set<Class<?>> interfaces) {
        LinkedHashSet<Class<?>> sorted = new LinkedHashSet<>(interfaces.size());
        processSuperinterface(null, interfaces, sorted);
        if (interfaces.size() != sorted.size()) {
            // Interface may not processed due to incomplete type closure
            Set<Class<?>> unprocessed = new HashSet<>(interfaces);
            unprocessed.removeAll(sorted);
            for (Class<?> unprocessedInterface : unprocessed) {
                processSuperinterface(unprocessedInterface, interfaces, sorted);
                sorted.add(unprocessedInterface);
            }
        }
        return sorted;
    }

    private static void processSuperinterface(Class<?> superinterface, Set<Class<?>> interfaces,
            LinkedHashSet<Class<?>> sorted) {
        for (Class<?> interfaceClass : interfaces) {
            if (isInterfaceExtending(interfaceClass, superinterface)) {
                processSuperinterface(interfaceClass, interfaces, sorted);
                sorted.add(interfaceClass);
            }
        }
    }

    private static boolean isInterfaceExtending(Class<?> interfaceClass, Class<?> superinterface) {
        if (interfaceClass.equals(superinterface)) {
            return false;
        } else if (superinterface == null) {
            return interfaceClass.getInterfaces().length == 0;
        } else {
            return Arrays2.contains(interfaceClass.getInterfaces(), superinterface);
        }
    }
}
