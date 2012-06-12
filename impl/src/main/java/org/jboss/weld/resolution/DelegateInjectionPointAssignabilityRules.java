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
package org.jboss.weld.resolution;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class DelegateInjectionPointAssignabilityRules extends BeanTypeAssignabilityRules {

    protected DelegateInjectionPointAssignabilityRules() {
    }

    private static final DelegateInjectionPointAssignabilityRules INSTANCE = new DelegateInjectionPointAssignabilityRules();

    public static DelegateInjectionPointAssignabilityRules instance() {
        return INSTANCE;
    }

    @Override
    protected boolean matches(TypeVariable<?> requiredType, Type beanType) {
        if (beanType instanceof TypeVariable<?>) {
            /*
             * the delegate type parameter and the bean type parameter are both type variables and the upper bound of the bean
             * type parameter is assignable to the upper bound, if any, of the delegate type parameter, or
             */
            TypeVariable<?> beanTypeVariable = (TypeVariable<?>) beanType;
            return areTypesInsideBounds(beanTypeVariable.getBounds(), EMPTY_TYPES, requiredType.getBounds());
        } else {
            /*
             * the delegate type parameter is a type variable, the bean type parameter is an actual type, and the actual type is
             * assignable to the upper bound, if any, of the type variable.
             */
            return isTypeInsideBounds(beanType, EMPTY_TYPES, requiredType.getBounds());
        }
    }
}
