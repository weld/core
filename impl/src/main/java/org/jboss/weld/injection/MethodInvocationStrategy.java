/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection;

import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.ObserverException;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.EventMetadata;

import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.event.CurrentEventMetadata;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Encapsulates various strategies for invoking a method injection point. The default implementation supports all the possible scenarios including dependency
 * injection to parameters. In addition, there are optimized implementations for commonly used scenarios such as:
 * <ul>
 * <li>an observer method with event parameter only</li>
 * <li>an observer method with event parameter and a {@link BeanManager} injection point (common in extensions)</li>
 * <li>an observer method with event parameter and an {@link EventMetadata} injection point</li>
 * </ul>
 *
 * @author Jozef Hartinger
 * @author Martin Kouba
 */
public abstract class MethodInvocationStrategy {

    private static final MethodInvocationStrategy DISPOSER_DEFAULT_STRATEGY = new DefaultMethodInvocationStrategy(IllegalArgumentException.class);

    private static final MethodInvocationStrategy DISPOSER_SIMPLE_STRATEGY = new SimpleMethodInvocationStrategy(IllegalArgumentException.class);

    private static final MethodInvocationStrategy OBSERVER_DEFAULT_STRATEGY = new DefaultMethodInvocationStrategy(ObserverException.class);

    private static final MethodInvocationStrategy OBSERVER_SIMPLE_STRATEGY = new SimpleMethodInvocationStrategy(ObserverException.class);

    private static final MethodInvocationStrategy OBSERVER_EVENT_PLUS_BEAN_MANAGER_STRATEGY = new SpecialParamPlusBeanManagerStrategy(ObserverException.class);

    protected final Class<? extends RuntimeException> exceptionTypeToThrow;

    MethodInvocationStrategy(Class<? extends RuntimeException> exceptionTypeToThrow) {
        this.exceptionTypeToThrow = exceptionTypeToThrow;
    }

    MethodInvocationStrategy() {
        this.exceptionTypeToThrow = null;
    }

    public static MethodInvocationStrategy forDisposer(MethodInjectionPoint<?, ?> method, BeanManagerImpl manager) {
        List<? extends ParameterInjectionPoint<?, ?>> parameters = method.getParameterInjectionPoints();
        if (parameters.size() == 1 && parameters.get(0).getAnnotated().isAnnotationPresent(Observes.class)) {
            return DISPOSER_SIMPLE_STRATEGY;
        } else {
            return DISPOSER_DEFAULT_STRATEGY;
        }
    }

    public static MethodInvocationStrategy forObserver(MethodInjectionPoint<?, ?> method, BeanManagerImpl manager) {
        List<? extends ParameterInjectionPoint<?, ?>> parameters = method.getParameterInjectionPoints();
        if (parameters.size() == 1 && parameters.get(0).getAnnotated().isAnnotationPresent(Observes.class)) {
            return OBSERVER_SIMPLE_STRATEGY;
        } else if (parameters.size() == 2) {
            if (parameters.get(0).getAnnotated().isAnnotationPresent(Observes.class)) {
                if (BeanManager.class.equals(parameters.get(1).getType())) {
                    return OBSERVER_EVENT_PLUS_BEAN_MANAGER_STRATEGY;
                } else if (EventMetadata.class.equals(parameters.get(1).getType())) {
                    return new EventPlusMetadataStrategy(manager);
                }
            }
        }
        return OBSERVER_DEFAULT_STRATEGY;
    }

    public abstract <T> void invoke(Object receiver, MethodInjectionPoint<?, ?> method, T instance, BeanManagerImpl manager,
            CreationalContext<?> creationalContext);

    /**
     * The default general-purpose invocation strategy.
     */
    private static class DefaultMethodInvocationStrategy extends MethodInvocationStrategy {

        public DefaultMethodInvocationStrategy(Class<? extends RuntimeException> exceptionTypeToThrow) {
            super(exceptionTypeToThrow);
        }

        @Override
        public <T> void invoke(Object receiver, MethodInjectionPoint<?, ?> method, T instance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            boolean release = creationalContext == null;
            if (release) {
                creationalContext = manager.createCreationalContext(null);
            }
            try {
                method.invoke(receiver, instance, manager, creationalContext, exceptionTypeToThrow);
            } finally {
                if (release) {
                    creationalContext.release();
                }
            }
        }
    }

    /**
     * Optimized invocation strategy that only supports methods with a single special parameter (e.g. the event parameter).
     */
    private static class SimpleMethodInvocationStrategy extends MethodInvocationStrategy {

        public SimpleMethodInvocationStrategy(Class<? extends RuntimeException> exceptionTypeToThrow) {
            super(exceptionTypeToThrow);
        }

        @Override
        public <T> void invoke(Object receiver, MethodInjectionPoint<?, ?> method, T instance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, instance, manager, creationalContext, exceptionTypeToThrow);
        }
    }

    /**
     * Optimized invocation strategy that supports methods with exactly two parameters: special parameter plus {@link BeanManager} injection point.
     */
    private static class SpecialParamPlusBeanManagerStrategy extends MethodInvocationStrategy {

        public SpecialParamPlusBeanManagerStrategy(Class<? extends RuntimeException> exceptionTypeToThrow) {
            super(exceptionTypeToThrow);
        }

        @Override
        public <T> void invoke(Object receiver, MethodInjectionPoint<?, ?> method, T instance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, new Object[] { instance, new BeanManagerProxy(manager) }, exceptionTypeToThrow);
        }
    }

    /**
     * Optimized invocation strategy that supports observer methods with exactly two parameters: event parameter plus {@link EventMetadata} injection point.
     */
    private static class EventPlusMetadataStrategy extends MethodInvocationStrategy {

        private final CurrentEventMetadata metadata;

        private EventPlusMetadataStrategy(BeanManagerImpl manager) {
            this.metadata = manager.getServices().get(CurrentEventMetadata.class);
        }

        @Override
        public <T> void invoke(Object receiver, MethodInjectionPoint<?, ?> method, T instance, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, new Object[] { instance, metadata.peek() }, ObserverException.class);
        }
    }

}
