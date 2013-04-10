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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * <p>
 * There are two possible scenarios in which we operate on a raw type (java class) in {@link HierarchyDiscovery}:
 * </p>
 *
 * <p>
 * Firstly, we are discovering hierarchy of a class bean with type parameters. In this scenario, we need to resolve the
 * {@link ParameterizedType} of the class instead of the raw type returned by the classloader. This is resolved by
 * {@link HierarchyDiscovery}.
 * </p>
 *
 * <p>
 * Secondly, we are working with a type of a java field, parameter or a method return type. If a generic class is declared
 * as a raw type, we want to preserve the fact that the type was raw because assignability rules in the CDI specification (5.2.3, 8.3.1 and 10.2.1)
 * recognize a raw type as a special case. Therefore, we <strong>do not</strong> want {@link HierarchyDiscovery} to resolve the type.
 * </p>
 *
 *<p>
 * This class is used as a wrapper that marks a raw type for the {@link HierarchyDiscovery} not to resolve the type (the second scenario - see above).
 *</p>
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class RawType<T> implements Type {

    public static Type wrap(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.getTypeParameters().length > 0) {
                return of(clazz);
            }
        }
        return type;
    }

    public static Type unwrap(Type type) {
        if (type instanceof RawType<?>) {
            return Reflections.<RawType<?>> cast(type).getType();
        }
        return type;
    }

    public static <T> RawType<T> of(Class<T> type) {
        return new RawType<T>(type);
    }

    private final Class<T> type;

    private RawType(Class<T> type) {
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "RawType [clazz=" + type + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RawType<?> other = (RawType<?>) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
