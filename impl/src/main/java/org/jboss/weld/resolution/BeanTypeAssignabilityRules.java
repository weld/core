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
import java.lang.reflect.WildcardType;

import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Implementation of the Section 5.2.4 of the CDI specification.
 *
 * @author Jozef Hartinger
 * @author Matus Abaffy
 */
public class BeanTypeAssignabilityRules extends AbstractAssignabilityRules {

    private static final AssignabilityRules INSTANCE = new BeanTypeAssignabilityRules();

    public static AssignabilityRules instance() {
        return INSTANCE;
    }

    protected BeanTypeAssignabilityRules() {
    }

    @Override
    public boolean matches(Type requiredType, Type beanType) {
        return matchesNoBoxing(Types.boxedType(requiredType), Types.boxedType(beanType));
    }

    public boolean matchesNoBoxing(Type requiredType, Type beanType) {
        /*
         * Special handling for array event types as eventType closure does not contain the type closure of array component type
         * this is here for backwards compatibility - see
         * ObserverMethodWithParametertizedTypeTest.testObserverMethodCanObserveArrayWildcard()
         */
        if (Types.isArray(requiredType) && Types.isArray(beanType)) {
            return matchesNoBoxing(Types.getArrayComponentType(requiredType), Types.getArrayComponentType(beanType));
        }
        if (requiredType instanceof Class<?>) {
            if (beanType instanceof Class<?>) {
                return matches((Class<?>) requiredType, (Class<?>) beanType);
            }
            if (beanType instanceof ParameterizedType) {
                return matches((Class<?>) requiredType, (ParameterizedType) beanType);
            }
        } else if (requiredType instanceof ParameterizedType) {
            if (beanType instanceof Class<?>) {
                return matches((Class<?>) beanType, (ParameterizedType) requiredType);
            }
            if (beanType instanceof ParameterizedType) {
                return matches((ParameterizedType) requiredType, (ParameterizedType) beanType);
            }
        }
        return false;
    }

    private boolean matches(Class<?> requiredType, Class<?> beanType) {
        return requiredType.equals(beanType);
    }

    /**
     * A parameterized bean type is considered assignable to a raw required type if the raw types
     * are identical and all type parameters of the bean type are either unbounded type variables or
     * java.lang.Object.
     * <p>
     * A raw bean type is considered assignable to a parameterized required type if the raw types are
     * identical and all type parameters of the required type are either unbounded type variables or
     * java.lang.Object.
     *
     */
    private boolean matches(Class<?> type1, ParameterizedType type2) {
        if (!type1.equals(Reflections.getRawType(type2))) {
            return false;
        }
        return Types.isArrayOfUnboundedTypeVariablesOrObjects(type2.getActualTypeArguments());
    }

    /**
     * A parameterized bean type is considered assignable to a parameterized required type if they have
     * identical raw type and for each parameter:
     */
    private boolean matches(ParameterizedType requiredType, ParameterizedType beanType) {
        if (!requiredType.getRawType().equals(beanType.getRawType())) {
            return false;
        }
        if (requiredType.getActualTypeArguments().length != beanType.getActualTypeArguments().length) {
            throw new IllegalArgumentException("Invalid argument combination " + requiredType + "; " + beanType);
        }
        for (int i = 0; i < requiredType.getActualTypeArguments().length; i++) {
            if (!parametersMatch(requiredType.getActualTypeArguments()[i], beanType.getActualTypeArguments()[i])) {
                return false;
            }
        }
        return true;
    }

    /*
     * Actual type parameters
     */

    protected boolean parametersMatch(Type requiredParameter, Type beanParameter) {
        if (Types.isActualType(requiredParameter) && Types.isActualType(beanParameter)) {
            /*
             * the required type parameter and the bean type parameter are actual types with identical raw
             * type, and, if the type is parameterized, the bean type parameter is assignable to the required
             * type parameter according to these rules, or
             */
            return matches(requiredParameter, beanParameter);
        }
        if (requiredParameter instanceof WildcardType && Types.isActualType(beanParameter)) {
            /*
             * the required type parameter is a wildcard, the bean type parameter is an actual type and the
             * actual type is assignable to the upper bound, if any, of the wildcard and assignable from the
             * lower bound, if any, of the wildcard, or
             */
            return parametersMatch((WildcardType) requiredParameter, beanParameter);
        }
        if (requiredParameter instanceof WildcardType && beanParameter instanceof TypeVariable<?>) {
            /*
             * the required type parameter is a wildcard, the bean type parameter is a type variable and the
             * upper bound of the type variable is assignable to or assignable from the upper bound, if any, of
             * the wildcard and assignable from the lower bound, if any, of the wildcard, or
             */
            return parametersMatch((WildcardType) requiredParameter, (TypeVariable<?>) beanParameter);
        }
        if (Types.isActualType(requiredParameter) && beanParameter instanceof TypeVariable<?>) {
            /*
             * the required type parameter is an actual type, the bean type parameter is a type variable and
             * the actual type is assignable to the upper bound, if any, of the type variable, or
             */
            return parametersMatch(requiredParameter, (TypeVariable<?>) beanParameter);
        }
        if (requiredParameter instanceof TypeVariable<?> && beanParameter instanceof TypeVariable<?>) {
            /*
             * the required type parameter and the bean type parameter are both type variables and the upper
             * bound of the required type parameter is assignable to the upper bound, if any, of the bean type
             * parameter
             */
            return parametersMatch((TypeVariable<?>) requiredParameter, (TypeVariable<?>) beanParameter);
        }
        return false;
    }

    protected boolean parametersMatch(WildcardType requiredParameter, Type beanParameter) {
        return (lowerBoundsOfWildcardMatch(beanParameter, requiredParameter)
                && upperBoundsOfWildcardMatch(requiredParameter, beanParameter));
    }

    protected boolean parametersMatch(WildcardType requiredParameter, TypeVariable<?> beanParameter) {
        Type[] beanParameterBounds = getUppermostTypeVariableBounds(beanParameter);
        if (!lowerBoundsOfWildcardMatch(beanParameterBounds, requiredParameter)) {
            return false;
        }

        Type[] requiredUpperBounds = requiredParameter.getUpperBounds();
        // upper bound of the type variable is assignable to OR assignable from the upper bound of the wildcard
        return (boundsMatch(requiredUpperBounds, beanParameterBounds) || boundsMatch(beanParameterBounds, requiredUpperBounds));
    }

    protected boolean parametersMatch(Type requiredParameter, TypeVariable<?> beanParameter) {
        for (Type bound : getUppermostTypeVariableBounds(beanParameter)) {
            if (!CovariantTypes.isAssignableFrom(bound, requiredParameter)) {
                return false;
            }
        }
        return true;
    }

    protected boolean parametersMatch(TypeVariable<?> requiredParameter, TypeVariable<?> beanParameter) {
        return boundsMatch(getUppermostTypeVariableBounds(beanParameter), getUppermostTypeVariableBounds(requiredParameter));
    }
}
