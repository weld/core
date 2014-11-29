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
 * Encapsulates various strategies for invoking an observer method. The default implementation supports all the possible scenarios including dependency
 * injection to parameters. In addition, there are optimized implementations for commonly used scenarios such as:
 * <ul>
 * <li>an observer method with event parameter only</li>
 * <li>an observer method with event parameter and a {@link BeanManager} injection point (common in extensions)</li>
 * <li>an observer method with event parameter and an {@link EventMetadata} injection point</li>
 * </ul>
 *
 * @author Jozef Hartinger
 *
 */
public abstract class ObserverMethodInvocationStrategy {

    public static ObserverMethodInvocationStrategy of(MethodInjectionPoint<?, ?> method, BeanManagerImpl manager) {
        List<? extends ParameterInjectionPoint<?, ?>> parameters = method.getParameterInjectionPoints();
        if (parameters.size() == 1 && parameters.get(0).getAnnotated().isAnnotationPresent(Observes.class)) {
            return SIMPLE_STRATEGY;
        } else if (parameters.size() == 2) {
            if (parameters.get(0).getAnnotated().isAnnotationPresent(Observes.class)) {
                if (BeanManager.class.equals(parameters.get(1).getType())) {
                    return EVENT_PLUS_BEAN_MANAGER_STRATEGY;
                } else if (EventMetadata.class.equals(parameters.get(1).getType())) {
                    return new EventPlusMetadataStrategy(manager);
                }
            }
        }
        return DEFAULT_STRATEGY;
    }

    public abstract <T, X> void notify(Object receiver, MethodInjectionPoint<T, X> method, T event, BeanManagerImpl manager, CreationalContext<?> creationalContext);

    /**
     * The default general-purpose invocation strategy.
     */
    private static final ObserverMethodInvocationStrategy DEFAULT_STRATEGY = new ObserverMethodInvocationStrategy() {
        @Override
        public <T, X> void notify(Object receiver, MethodInjectionPoint<T, X> method, T event, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            boolean release = creationalContext == null;
            if (release) {
                creationalContext = manager.createCreationalContext(null);
            }
            try {
                method.invoke(receiver, event, manager, creationalContext, ObserverException.class);
            } finally {
                if (release) {
                    creationalContext.release();
                }
            }
        }
    };

    /**
     * Optimized invocation strategy that only supports observer methods with a single parameter (the event parameter).
     */
    private static final ObserverMethodInvocationStrategy SIMPLE_STRATEGY = new ObserverMethodInvocationStrategy() {
        @Override
        public <T, X> void notify(Object receiver, MethodInjectionPoint<T, X> method, T event, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, event, manager, creationalContext, ObserverException.class);
        }
    };

    /**
     * Optimized invocation strategy that supports observer methods with exactly two parameters: event parameter plus {@link BeanManager} injection point.
     */
    private static final ObserverMethodInvocationStrategy EVENT_PLUS_BEAN_MANAGER_STRATEGY = new ObserverMethodInvocationStrategy() {
        @Override
        public <T, X> void notify(Object receiver, MethodInjectionPoint<T, X> method, T event, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, new Object[] { event, new BeanManagerProxy(manager) }, ObserverException.class);
        }
    };

    /**
     * Optimized invocation strategy that supports observer methods with exactly two parameters: event parameter plus {@link EventMetadata} injection point.
     */
    private static class EventPlusMetadataStrategy extends ObserverMethodInvocationStrategy {

        private final CurrentEventMetadata metadata;

        private EventPlusMetadataStrategy(BeanManagerImpl manager) {
            this.metadata = manager.getServices().get(CurrentEventMetadata.class);
        }

        @Override
        public <T, X> void notify(Object receiver, MethodInjectionPoint<T, X> method, T event, BeanManagerImpl manager, CreationalContext<?> creationalContext) {
            method.invoke(receiver, new Object[] { event, metadata.peek() }, ObserverException.class);
        }
    }
}
