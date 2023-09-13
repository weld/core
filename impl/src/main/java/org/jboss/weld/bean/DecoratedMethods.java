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
package org.jboss.weld.bean;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resolution.CovariantTypes;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.Types;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Resolves decorator method for a given business method.
 *
 * @author Jozef Hartinger
 *
 */
class DecoratedMethods {

    /*
     * We use this marker because ConcurrentMap does not support null values but we want to keep track of
     * methods for which there is no decorated method declared by the decorator.
     */
    private static final Object NULL_MARKER = new Object();

    private final Set<InvokableAnnotatedMethod<?>> decoratedTypeMethods;
    private final ConcurrentMap<Method, Object> cache;

    DecoratedMethods(BeanManagerImpl manager, WeldDecorator<?> decorator) {
        this.decoratedTypeMethods = Decorators.getDecoratorMethods(manager, decorator);
        this.cache = new ConcurrentHashMap<Method, Object>();
    }

    public InvokableAnnotatedMethod<?> getDecoratedMethod(Method method) {
        if (!cache.containsKey(method)) {
            // this is not atomic and we may end up doing method lookup more than once - which is fine
            cache.putIfAbsent(method, findMatchingDecoratedMethod(method));
        }
        Object value = cache.get(method);
        if (value == NULL_MARKER) {
            return null;
        } else {
            return Reflections.cast(value);
        }
    }

    private Object findMatchingDecoratedMethod(Method method) {
        // First try to find the same method
        for (InvokableAnnotatedMethod<?> decoratedMethod : decoratedTypeMethods) {
            if (decoratedMethod.getJavaMember().equals(method)) {
                return decoratedMethod;
            }
        }
        // Then try to find all matching methods
        List<InvokableAnnotatedMethod<?>> matching = new ArrayList<InvokableAnnotatedMethod<?>>();
        for (InvokableAnnotatedMethod<?> decoratedMethod : decoratedTypeMethods) {
            if (matches(decoratedMethod, method)) {
                matching.add(decoratedMethod);
            }
        }
        if (matching.isEmpty()) {
            // No match
            return NULL_MARKER;
        } else if (matching.size() == 1) {
            // If there is only one matching method no further action is needed
            return matching.get(0);
        }
        // Choose the most specific method
        // This does not meet all requirements of JLS but it should work in most cases
        // See also JLS Java SE 8 Edition, 15.12.2.5 Choosing the Most Specific Method
        InvokableAnnotatedMethod<?> mostSpecific = matching.get(0);
        for (int i = 1; i < matching.size(); i++) {
            InvokableAnnotatedMethod<?> candidate = matching.get(i);
            if (isMoreSpecific(candidate, mostSpecific)) {
                mostSpecific = candidate;
            }
        }
        return mostSpecific;
    }

    private boolean matches(InvokableAnnotatedMethod<?> decoratedMethod, Method candidate) {
        if (candidate.getParameterCount() != decoratedMethod.getParameters().size()) {
            return false;
        }
        if (!candidate.getName().equals(decoratedMethod.getJavaMember().getName())) {
            return false;
        }
        for (int i = 0; i < candidate.getParameterCount(); i++) {
            Type decoratedMethodParamType = decoratedMethod.getJavaMember().getGenericParameterTypes()[i];
            Type candidateParamType = candidate.getGenericParameterTypes()[i];
            if (Types.containsTypeVariable(decoratedMethodParamType) || Types.containsTypeVariable(candidateParamType)) {
                if (!decoratedMethod.getJavaMember().getParameterTypes()[i]
                        .isAssignableFrom(candidate.getParameterTypes()[i])) {
                    return false;
                }
            } else {
                if (!CovariantTypes.isAssignableFrom(decoratedMethodParamType, candidateParamType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isMoreSpecific(InvokableAnnotatedMethod<?> candidate, InvokableAnnotatedMethod<?> mostSpecific) {
        for (int i = 0; i < candidate.getJavaMember().getGenericParameterTypes().length; i++) {
            if (Types.isMoreSpecific(candidate.getJavaMember().getGenericParameterTypes()[i],
                    mostSpecific.getJavaMember().getGenericParameterTypes()[i])) {
                return true;
            }
        }
        return false;
    }

}
