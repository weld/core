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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Decorators;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Resolves decorator method for a given business method.
 *
 * @author Jozef Hartinger
 *
 */
class DecoratedMethods {

    /*
     *  We use this marker because ConcurrentMap does not support null values but we want to keep track of
     *  methods for which there is no decorated method declared by the decorator.
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
        for (InvokableAnnotatedMethod<?> decoratedMethod : decoratedTypeMethods) {
            if (decoratedMethod.getJavaMember().equals(method)) {
                return decoratedMethod;
            }
        }
        for (InvokableAnnotatedMethod<?> decoratedMethod : decoratedTypeMethods) {
            if (matches(decoratedMethod, method)) {
                return decoratedMethod;
            }
        }
        return NULL_MARKER;
    }

    private boolean matches(InvokableAnnotatedMethod<?> decoratedMethod, Method candidate) {
        if (candidate.getParameterTypes().length != decoratedMethod.getParameters().size()) {
            return false;
        }
        if (!candidate.getName().equals(decoratedMethod.getJavaMember().getName())) {
            return false;
        }
        for (int i = 0; i < candidate.getParameterTypes().length; i++) {
            if (!decoratedMethod.getJavaMember().getParameterTypes()[i].isAssignableFrom(candidate.getParameterTypes()[i])) {
                return false;
            }
        }
        return true;
    }
}
