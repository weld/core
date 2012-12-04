/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.util.Types;
import org.jboss.weld.util.collections.ArraySet;

/**
 * @author Weld Community
 * @author Ales Justin
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class HierarchyDiscovery {

    private final Map<Class<?>, Type> types;
    private final Map<TypeVariable<?>, Type> resolvedTypeVariables;

    public HierarchyDiscovery(Type type) {
        this.types = new HashMap<Class<?>, Type>();
        this.resolvedTypeVariables = new HashMap<TypeVariable<?>, Type>();
        discoverTypes(type);
    }

    public Set<Type> getTypeClosure() {
        return new ArraySet<Type>(this.types.values());
    }

    public Map<Class<?>, Type> getTypeMap() {
        return types;
    }

    private void discoverTypes(Type type) {
        if (type instanceof RawType<?>) {
            RawType<?> rawType = (RawType<?>) type;
            this.types.put(rawType.getType(), rawType.getType());
            discoverFromClass(rawType.getType());
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            this.types.put(clazz, Types.resolveType(clazz));
            discoverFromClass(clazz);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Type genericComponentType = arrayType.getGenericComponentType();
            Class<?> rawComponentType = Reflections.getRawType(genericComponentType);
            if (rawComponentType != null) {
                Class<?> arrayClass = Array.newInstance(rawComponentType, 0).getClass();
                this.types.put(arrayClass, type);
                discoverFromClass(arrayClass);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = (parameterizedType).getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> clazz = (Class<?>) rawType;
                processTypeVariables(clazz.getTypeParameters(), parameterizedType.getActualTypeArguments());
                this.types.put(clazz, type);
                discoverFromClass(clazz);
            }
        }
    }

    private void discoverFromClass(Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            discoverTypes(processAndResolveType(clazz.getGenericSuperclass(), clazz.getSuperclass()));
        }
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            discoverTypes(processAndResolveType(genericInterfaces[i], interfaces[i]));
        }
    }

    private Type processAndResolveType(Type superclass, Class<?> rawSuperclass) {
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperclass = (ParameterizedType) superclass;
            processTypeVariables(rawSuperclass.getTypeParameters(), parameterizedSuperclass.getActualTypeArguments());
            return resolveType(parameterizedSuperclass);
        } else if (superclass instanceof Class<?>) {
            // this is not a parameterized type, nothing to resolve
            return superclass;
        }
        throw new RuntimeException("WTF!");
    }

    /*
     * Processing part. Every type variable is mapped to the actual type in the resolvedTypeVariablesMap. This map is used later
     * on for resolving types.
     */

    private void processTypeVariables(TypeVariable<?>[] variables, Type[] values) {
        for (int i = 0; i < variables.length; i++) {
            processTypeVariable(variables[i], values[i]);
        }
    }

    private void processTypeVariable(TypeVariable<?> variable, Type value) {
        if (value instanceof TypeVariable<?>) {
            value = resolveType((TypeVariable<?>) value);
        }
        this.resolvedTypeVariables.put(variable, value);
    }

    /*
     * Resolving part. Using resolvedTypeVariables map which was prepared in the processing part.
     */

    /**
     * Resolves a given type variable. This is achieved by a lookup in the {@link #resolvedTypeVariables} map.
     */
    public Type resolveType(TypeVariable<?> variable) {
        Type resolvedType = this.resolvedTypeVariables.get(variable);
        if (resolvedType == null) {
            return variable; // we are not able to resolve
        }
        return resolvedType;
    }

    /**
     * Resolves a given parameterized type. If the parameterized type contains no type variables it is returned untouched.
     * Otherwise, a new {@link ParameterizedType} instance is returned in which each type variable is resolved using
     * {@link #resolveType(TypeVariable)}.
     */
    public Type resolveType(ParameterizedType type) {
        Type[] typeArguments = type.getActualTypeArguments();

        /*
         * Indicates whether we managed to resolve any of type arguments. If we did not then there is no need to create a new
         * ParameterizedType with the old parameters. Instead, we return the original type.
         */
        boolean modified = false;

        for (int i = 0; i < typeArguments.length; i++) {
            Type unresolvedType = typeArguments[i];
            if (unresolvedType instanceof TypeVariable<?>) {
                typeArguments[i] = resolveType((TypeVariable<?>) unresolvedType);
            }
            if (unresolvedType instanceof ParameterizedType) {
                typeArguments[i] = resolveType((ParameterizedType) unresolvedType);
            }
            if (typeArguments[i] != unresolvedType) { // This identity check is intentional. A different identity indicates that the type argument was resolved.
                modified = true;
            }
        }

        if (modified) {
            return new ParameterizedTypeImpl(type.getRawType(), typeArguments, type.getOwnerType());
        } else {
            return type;
        }
    }

    public Type resolveType(GenericArrayType type) {
        Type genericComponentType = type.getGenericComponentType();
        // try to resolve the type
        Type resolvedType = genericComponentType;
        if (genericComponentType instanceof TypeVariable<?>) {
            resolvedType = resolveType((TypeVariable<?>) genericComponentType);
        }
        if (genericComponentType instanceof ParameterizedType) {
            resolvedType = resolveType((ParameterizedType) genericComponentType);
        }
        if (genericComponentType instanceof GenericArrayType) {
            resolvedType = resolveType((GenericArrayType) genericComponentType);
        }
        /*
         * If the generic component type resolved to a class (e.g. String) we return [Ljava.lang.String; (the class representing the
         * array) instead of GenericArrayType with String as its generic component type.
         */
        if (resolvedType instanceof Class<?>) {
            Class<?> componentClass = (Class<?>) resolvedType;
            return Array.newInstance(componentClass, 0).getClass();
        }
        /*
         * This identity check is intentional. If the identity is different it indicates that we succeeded in resolving the type
         * and a new GenericArrayType with resolved generic component type is returned. Otherwise, we were not able to resolve
         * the type and therefore we do not create a new GenericArrayType.
         */
        if (resolvedType == genericComponentType) {
            return type;
        } else {
            return new GenericArrayTypeImpl(resolvedType);
        }
    }

    public Type resolveType(Type type) {
        if (type instanceof ParameterizedType) {
            return resolveType((ParameterizedType) type);
        }
        if (type instanceof TypeVariable<?>) {
            return resolveType((TypeVariable<?>) type);
        }
        if (type instanceof GenericArrayType) {
            return resolveType((GenericArrayType) type);
        }
        return type;
    }
}
