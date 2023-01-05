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

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Set;

/**
 * Base class for AssignabilityRules implementations.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractAssignabilityRules implements AssignabilityRules {

    @Override
    public boolean matches(Set<Type> requiredTypes, Set<Type> beanTypes) {
        for (Type requiredType : requiredTypes) {
            if (matches(requiredType, beanTypes)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(Type requiredType, Set<? extends Type> beanTypes) {
        for (Type beanType : beanTypes) {
            if (matches(requiredType, beanType)) {
                return true;
            }
        }
        return false;
    }

    /*
     * TypeVariable bounds are treated specially - CDI assignability rules are applied.
     * Standard Java covariant assignability rules are applied to all other types of bounds.
     * This is not explicitly mentioned in the specification but is implied.
     */
    static Type[] getUppermostTypeVariableBounds(TypeVariable<?> bound) {
        if (bound.getBounds()[0] instanceof TypeVariable<?>) {
            return getUppermostTypeVariableBounds((TypeVariable<?>) bound.getBounds()[0]);
        }
        return bound.getBounds();
    }

    private Type[] getUppermostBounds(Type[] bounds) {
        // if a type variable (or wildcard) declares a bound which is a type variable, it can declare no other bound
        if (bounds[0] instanceof TypeVariable<?>) {
            return getUppermostTypeVariableBounds((TypeVariable<?>) bounds[0]);
        }
        return bounds;
    }

    /**
     * Returns <tt>true</tt> iff for each upper bound T, there is at least one bound from <tt>stricterUpperBounds</tt>
     * assignable to T. This reflects that <tt>stricterUpperBounds</tt> are at least as strict as <tt>upperBounds</tt> are.
     * <p>
     * Arguments passed to this method must be legal java bounds, i.e. bounds returned by {@link TypeVariable#getBounds()},
     * {@link WildcardType#getUpperBounds()} or {@link WildcardType#getLowerBounds()}.
     */
    protected boolean boundsMatch(Type[] upperBounds, Type[] stricterUpperBounds) {
        // getUppermostBounds to make sure that both arrays of bounds contain ONLY ACTUAL TYPES! otherwise, the CovariantTypes
        // assignability rules do not reflect our needs
        upperBounds = getUppermostBounds(upperBounds);
        stricterUpperBounds = getUppermostBounds(stricterUpperBounds);
        for (Type upperBound : upperBounds) {
            if (!CovariantTypes.isAssignableFromAtLeastOne(upperBound, stricterUpperBounds)) {
                return false;
            }
        }
        return true;
    }

    protected boolean lowerBoundsOfWildcardMatch(Type parameter, WildcardType requiredParameter) {
        return lowerBoundsOfWildcardMatch(new Type[] { parameter }, requiredParameter);
    }

    protected boolean lowerBoundsOfWildcardMatch(Type[] beanParameterBounds, WildcardType requiredParameter) {
        if (requiredParameter.getLowerBounds().length > 0) {
            Type[] lowerBounds = requiredParameter.getLowerBounds();
            if (!boundsMatch(beanParameterBounds, lowerBounds)) {
                return false;
            }
        }
        return true;
    }

    protected boolean upperBoundsOfWildcardMatch(WildcardType requiredParameter, Type parameter) {
        return boundsMatch(requiredParameter.getUpperBounds(), new Type[] { parameter });
    }
}
