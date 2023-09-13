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
import org.jboss.weld.util.collections.ImmutableSet;

/**
 * Utility class that discovers transitive type closure of a given type.
 *
 * @author Weld Community
 * @author Ales Justin
 * @author Marko Luksa
 * @author Jozef Hartinger
 */
public class HierarchyDiscovery {

    /**
     * Performs base type normalization before hierarchy discovery is performed.
     * <p>
     * Type normalization only affects parameterized types (e.g. public class <tt>Foo&lt;T extends Serializable&gt;</tt>)
     * that are used in form of a raw type (e.g. <tt>Foo.class</tt>). During the process of type normalization, this raw type
     * (<tt>Foo.class</tt> is instance of <tt>Class<?></tt>) is replaced by a canonical version of the type which in this case
     * would be the parameterized type of <tt>Foo&lt;T extends Serializable&gt;</tt>
     * <p>
     * Base type normalization means that only the base type, which is the input for hierarchy discovery, is normalized.
     * Other types discovered during hierarchy discovery are never normalized even if a raw form of a parameterized type
     * is discovered.
     * <p>
     * A user of this class should recognize whether base type normalization is required and set the <tt>normalize</tt>
     * parameter accordingly.
     * <p>
     * In the realm of CDI there is only a single use-case for base type normalization. That is resolving bean types of
     * a bean defined as a class (managed and session beans). Here, e.g. discovered <tt>Foo.class</tt> needs to be normalized as
     * the correct CDI bean type is <tt>Foo&lt;T extends Serializable&gt;</tt>, not <tt>Foo.class</tt>.
     * <p>
     * In other cases, the complete generic information of the base type is known and thus base type normalization should
     * not be used so that it does not cover intentionally declared raw types (e.g. an injection point with a raw type should
     * be recognized as an injection point with a raw type, not it's canonical version).
     * This covers:
     * <ul>
     * <li>type closure of an injection point or delegate</li>
     * <li>type closure of a producer field type</li>
     * <li>type closure of a producer method return type</li>
     * <li>type closure of a parameter type (observer, initializer, producer and disposer methods)</li>
     * <li>type closure of an event</li>
     * </ul>
     **/
    public static HierarchyDiscovery forNormalizedType(Type type) {
        return new HierarchyDiscovery(Types.getCanonicalType(type));
    }

    private final Map<Class<?>, Type> types;
    private final Map<TypeVariable<?>, Type> resolvedTypeVariables;
    private final TypeResolver resolver;
    private final Set<Type> typeClosure;

    /**
     * Constructs a new {@link HierarchyDiscovery} instance.
     *
     * @param type the type whose hierarchy will be discovered
     */
    public HierarchyDiscovery(Type type) {
        this(type, new TypeResolver(new HashMap<TypeVariable<?>, Type>()));
    }

    public HierarchyDiscovery(Type type, TypeResolver resolver) {
        this.types = new HashMap<Class<?>, Type>();
        this.resolver = resolver;
        this.resolvedTypeVariables = resolver.getResolvedTypeVariables();
        discoverTypes(type, false);
        this.typeClosure = ImmutableSet.copyOf(types.values());
    }

    public Set<Type> getTypeClosure() {
        return typeClosure;
    }

    public Map<Class<?>, Type> getTypeMap() {
        return types;
    }

    protected void discoverTypes(Type type, boolean rawGeneric) {
        if (!rawGeneric) {
            rawGeneric = Types.isRawGenericType(type);
        }
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            this.types.put(clazz, clazz);
            discoverFromClass(clazz, rawGeneric);
        } else if (rawGeneric) {
            discoverTypes(Reflections.getRawType(type), rawGeneric);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) type;
            Type genericComponentType = arrayType.getGenericComponentType();
            Class<?> rawComponentType = Reflections.getRawType(genericComponentType);
            if (rawComponentType != null) {
                Class<?> arrayClass = Array.newInstance(rawComponentType, 0).getClass();
                this.types.put(arrayClass, type);
                discoverFromClass(arrayClass, rawGeneric);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = (parameterizedType).getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> clazz = (Class<?>) rawType;
                processTypeVariables(clazz.getTypeParameters(), parameterizedType.getActualTypeArguments());
                this.types.put(clazz, type);
                discoverFromClass(clazz, rawGeneric);
            }
        }
    }

    protected void discoverFromClass(Class<?> clazz, boolean rawGeneric) {
        if (clazz.getSuperclass() != null) {
            discoverTypes(processAndResolveType(clazz.getGenericSuperclass(), clazz.getSuperclass()), rawGeneric);
        }
        discoverInterfaces(clazz, rawGeneric);
    }

    protected void discoverInterfaces(Class<?> clazz, boolean rawGeneric) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        Class<?>[] interfaces = clazz.getInterfaces();
        if (genericInterfaces.length == interfaces.length) {
            // this branch should execute every time!
            for (int i = 0; i < interfaces.length; i++) {
                discoverTypes(processAndResolveType(genericInterfaces[i], interfaces[i]), rawGeneric);
            }
        }
    }

    protected Type processAndResolveType(Type superclass, Class<?> rawSuperclass) {
        if (superclass instanceof ParameterizedType) {
            ParameterizedType parameterizedSuperclass = (ParameterizedType) superclass;
            processTypeVariables(rawSuperclass.getTypeParameters(), parameterizedSuperclass.getActualTypeArguments());
            return resolveType(parameterizedSuperclass);
        } else if (superclass instanceof Class<?>) {
            // this is not a parameterized type, nothing to resolve
            return superclass;
        }
        throw new RuntimeException("Unexpected type: " + superclass);
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
            value = resolveType(value);
        }
        this.resolvedTypeVariables.put(variable, value);
    }

    /*
     * Resolving part. Using resolvedTypeVariables map which was prepared in the processing part.
     */

    public Type resolveType(Type type) {
        if (type instanceof Class) {
            Type resolvedType = types.get(type);
            if (resolvedType != null) {
                return resolvedType;
            }
        }
        return resolver.resolveType(type);
    }

    public TypeResolver getResolver() {
        return resolver;
    }
}
