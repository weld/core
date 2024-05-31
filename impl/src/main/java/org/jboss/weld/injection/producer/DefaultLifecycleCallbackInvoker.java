/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import java.lang.reflect.Method;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.AnnotatedMethod;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.collections.ImmutableList;
import org.jboss.weld.util.reflection.Reflections;

/**
 * If the component is not intercepted this implementation takes care of invoking its lifecycle callback methods. If the
 * component is interception, {@link PostConstruct} / {@link PreDestroy} invocation is delegated to the intercepting proxy.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class DefaultLifecycleCallbackInvoker<T> implements LifecycleCallbackInvoker<T> {

    public static <T> DefaultLifecycleCallbackInvoker<T> of(EnhancedAnnotatedType<T> type) {
        return new DefaultLifecycleCallbackInvoker<T>(type);
    }

    private final List<Method> accessiblePostConstructMethods;
    private final List<Method> accessiblePreDestroyMethods;

    public DefaultLifecycleCallbackInvoker(EnhancedAnnotatedType<T> type) {
        this.accessiblePostConstructMethods = initMethodList(BeanMethods.getPostConstructMethods(type));
        this.accessiblePreDestroyMethods = initMethodList(BeanMethods.getPreDestroyMethods(type));
    }

    private List<Method> initMethodList(List<? extends AnnotatedMethod<?>> methods) {
        return methods.stream()
                .map((method) -> Reflections.getAccessibleCopyOfMember(method.getJavaMember()))
                .collect(ImmutableList.collector());
    }

    @Override
    public void postConstruct(T instance, Instantiator<T> instantiator) {
        // this may be null for NonProducibleInjectionTarget
        if (instantiator != null && instantiator.hasInterceptorSupport()) {
            InterceptionUtils.executePostConstruct(instance);
        } else {
            invokeMethods(accessiblePostConstructMethods, instance);
        }
    }

    @Override
    public void preDestroy(T instance, Instantiator<T> instantiator) {
        // this may be null for NonProducibleInjectionTarget
        if (instantiator != null && instantiator.hasInterceptorSupport()) {
            InterceptionUtils.executePredestroy(instance);
        } else {
            invokeMethods(accessiblePreDestroyMethods, instance);
        }
    }

    private void invokeMethods(List<Method> methods, T instance) {
        for (Method method : methods) {
            try {
                method.invoke(instance);
            } catch (Exception e) {
                throw BeanLogger.LOG.invocationError(method, instance, e);
            }
        }
    }

    @Override
    public boolean hasPreDestroyMethods() {
        return !accessiblePreDestroyMethods.isEmpty();
    }

    @Override
    public boolean hasPostConstructMethods() {
        return !accessiblePostConstructMethods.isEmpty();
    }
}
