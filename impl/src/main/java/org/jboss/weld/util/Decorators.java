/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.util;

import static org.jboss.weld.logging.messages.BeanMessage.UNABLE_TO_PROCESS;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.enhanced.MethodSignature;
import org.jboss.weld.annotated.enhanced.jlr.MethodSignatureImpl;
import org.jboss.weld.annotated.runtime.InvokableAnnotatedMethod;
import org.jboss.weld.bean.WeldDecorator;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Helper class for {@link javax.enterprise.inject.spi.Decorator} inspections.
 *
 * @author Marius Bogoevici
 */
public class Decorators {

    public static Map<MethodSignature, InvokableAnnotatedMethod<?>> getDecoratorMethods(BeanManagerImpl beanManager, Set<Type> decoratedTypes, EnhancedAnnotatedType<?> decoratorClass) {
        List<EnhancedAnnotatedMethod<?, ?>> decoratedMethods = Decorators.getDecoratedMethods(beanManager, decoratedTypes);
        Map<MethodSignature, InvokableAnnotatedMethod<?>> decoratorMethods = new HashMap<MethodSignature, InvokableAnnotatedMethod<?>>();
        for (EnhancedAnnotatedMethod<?, ?> method : decoratorClass.getEnhancedMethods()) {
            MethodSignatureImpl methodSignature = new MethodSignatureImpl(method);
            for (EnhancedAnnotatedMethod<?, ?> decoratedMethod : decoratedMethods) {
                if (new MethodSignatureImpl(decoratedMethod).equals(methodSignature)) {
                    decoratorMethods.put(methodSignature, InvokableAnnotatedMethod.of(decoratedMethod.slim()));
                }
            }
        }
        return decoratorMethods;
    }

    public static List<EnhancedAnnotatedMethod<?, ?>> getDecoratedMethods(BeanManagerImpl beanManager, Set<Type> decoratedTypes) {
        List<EnhancedAnnotatedMethod<?, ?>> methods = new ArrayList<EnhancedAnnotatedMethod<?, ?>>();
        for (Type type : decoratedTypes) {
            EnhancedAnnotatedType<?> weldClass = getWeldClassOfDecoratedType(beanManager, type);
            for (EnhancedAnnotatedMethod<?, ?> method : weldClass.getEnhancedMethods()) {
                if (!methods.contains(method)) {
                    methods.add(method);
                }
            }
        }
        return methods;
    }

    public static EnhancedAnnotatedType<?> getWeldClassOfDecoratedType(BeanManagerImpl beanManager, Type type) {
        if (type instanceof Class<?>) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) type);
        }
        if (type instanceof ParameterizedType && (((ParameterizedType) type).getRawType() instanceof Class)) {
            return beanManager.createEnhancedAnnotatedType((Class<?>) ((ParameterizedType) type).getRawType());
        }
        throw new IllegalStateException(UNABLE_TO_PROCESS, type);
    }

    public static <T> InvokableAnnotatedMethod<?> findDecoratorMethod(WeldDecorator<T> decorator, Map<MethodSignature, InvokableAnnotatedMethod<?>> decoratorMethods, Method method) {
        // try the signature first, might be simpler
        MethodSignature key = new MethodSignatureImpl(method);
        InvokableAnnotatedMethod<?> foundMethod = decoratorMethods.get(key);
        if (foundMethod != null) {
            return foundMethod;
        }
        // try all methods
        for (InvokableAnnotatedMethod<?> decoratorMethod : decoratorMethods.values()) {
            if (method.getParameterTypes().length == decoratorMethod.getParameters().size()
                    && method.getName().equals(decoratorMethod.getJavaMember().getName())) {
                boolean parameterMatch = true;
                for (int i = 0; parameterMatch && i < method.getParameterTypes().length; i++) {
                    parameterMatch = parameterMatch && decoratorMethod.getJavaMember().getParameterTypes()[i].isAssignableFrom(method.getParameterTypes()[i]);
                }
                if (parameterMatch) {
                    return decoratorMethod;
                }
            }
        }

        return null;
    }
}
