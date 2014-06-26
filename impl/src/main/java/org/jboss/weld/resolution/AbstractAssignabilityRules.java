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
import java.util.Set;

import org.jboss.weld.exceptions.UnsupportedOperationException;

/**
 * Base class for AssignabilityRules implementations.
 *
 * @author Jozef Hartinger
 *
 */
public abstract class AbstractAssignabilityRules implements AssignabilityRules {

    @Override
    public boolean isAssignableFrom(Type type1, Type type2) {
        throw new UnsupportedOperationException("Not supported anymore.");
    }

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
    protected Type[] getUppermostTypeVariableBounds(TypeVariable<?> bound) {
        if (bound.getBounds()[0] instanceof TypeVariable<?>) {
            return getUppermostTypeVariableBounds((TypeVariable<?>) bound.getBounds()[0]);
        }
        return bound.getBounds();
    }
}
