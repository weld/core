/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.resolution;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.jboss.weld.util.reflection.Reflections;

/**
 *
 * A util for deciding whether two {@link Type}s are the same (not necessarily equal in the java sense).
 *
 * @author Matus Abaffy
 */
public class TypeEqualitySpecializationUtils {

    private TypeEqualitySpecializationUtils() {
    }

    public static boolean areTheSame(Type type1, Type type2) {
        if (type1.equals(type2)) {
            return true;
        }
        if (type1 instanceof ParameterizedType && type2 instanceof ParameterizedType) {
            return areTheSame((ParameterizedType) type1, (ParameterizedType) type2);
        }
        if (type1 instanceof TypeVariable<?> && type2 instanceof TypeVariable<?>) {
            return areTheSame((TypeVariable<?>) type1, (TypeVariable<?>) type2);
        }
        return false;
    }

    protected static boolean areTheSame(ParameterizedType type1, ParameterizedType type2) {
        if (!type1.getRawType().equals(type2.getRawType())) {
            return false;
        }
        return areTheSame(type1.getActualTypeArguments(), type2.getActualTypeArguments());
    }

    protected static boolean areTheSame(TypeVariable<?> type1, TypeVariable<?> type2) {
        List<Type> bounds1 = removeRedundantBounds(type1.getBounds());
        List<Type> bounds2 = removeRedundantBounds(type2.getBounds());

        if (bounds1.size() != bounds2.size()) {
            return false;
        }

        // each bound from bounds_i have to be in bounds_j, i.e. members of bounds1 have to be the same as members of bounds2
        for (Type type : bounds1) {
            if (!isTheSameAsSomeOf(type, bounds2)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isTheSameAsSomeOf(Type bound1, List<Type> bounds2) {
        for (Type type : bounds2) {
            if (areTheSame(bound1, type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all instances of {@link Class} and {@link ParameterizedType} for which a subtype is present in 'bounds'
     */
    private static List<Type> removeRedundantBounds(final Type[] bounds) {
        if (bounds.length == 1) {
            return Collections.singletonList(bounds[0]);
        }
        // if TypeVariable S has a bound T that is a TypeVariable, than S cannot have any other bounds, i.e. bounds.lenght == 1

        List<Type> result = new LinkedList<Type>();
        for (int i = 0; i < bounds.length; i++) {
            boolean isRedundant = false;
            for (int j = 0; j < bounds.length && i != j; j++) {
                if (Reflections.getRawType(bounds[i]).isAssignableFrom(Reflections.getRawType(bounds[j]))) {
                    isRedundant = true;
                    break;
                }
            }
            if (!isRedundant) {
                result.add(bounds[i]);
            }
        }

        return result;
    }

    protected static boolean areTheSame(Type[] types1, Type[] types2) {
        if (types1.length != types2.length) {
            return false;
        }
        for (int i = 0; i < types1.length; i++) {
            if (!areTheSame(types1[i], types2[i])) {
                return false;
            }
        }
        return true;
    }
}
