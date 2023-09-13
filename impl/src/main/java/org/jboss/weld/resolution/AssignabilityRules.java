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
import java.util.Set;

public interface AssignabilityRules {

    /**
     * Check whether at least one of the requiredTypes matches a type in beanTypes
     *
     * @param requiredTypes the requiredTypes
     * @param beanTypes the beanTypes
     * @return can we assign some type from requiredTypes to some type in beanTypes
     */
    boolean matches(Set<Type> requiredTypes, Set<Type> beanTypes);

    /**
     * Check whether requiredType matches a type in beanTypes
     *
     * @param requiredType the requiredType
     * @param beanTypes the beanTypes
     * @return can we assign requiredType to some type in beanTypes
     */
    boolean matches(Type requiredType, Set<? extends Type> beanTypes);

    boolean matches(Type requiredType, Type beanType);

}
