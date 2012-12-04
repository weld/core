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
        if (!containsUnresoledTypeVariable(type)) {
            return type;
        }
        Type[] unresolvedTypes = type.getActualTypeArguments();
        Type[] resolvedTypes = new Type[unresolvedTypes.length];
        for (int i = 0; i < unresolvedTypes.length; i++) {
            Type unresolvedType = unresolvedTypes[i];
            if (unresolvedType instanceof TypeVariable<?>) {
                resolvedTypes[i] = resolveType((TypeVariable<?>) unresolvedType);
            } else {
                resolvedTypes[i] = unresolvedTypes[i];
            }
        }
        return new ParameterizedTypeImpl(type.getRawType(), resolvedTypes, type.getOwnerType());
    }

    /**
     * Indicates whether a given {@link ParameterizedType} contains an unresolved type variable.
     */
    public boolean containsUnresoledTypeVariable(ParameterizedType type) {
        for (Type typeArgument : type.getActualTypeArguments()) {
            if (typeArgument instanceof TypeVariable<?>) {
                return true;
            }
        }
        return false;
    }
}
