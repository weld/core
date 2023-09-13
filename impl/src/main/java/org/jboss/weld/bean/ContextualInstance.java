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

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Convenience facade for using {@link ContextualInstanceStrategy}.
 *
 * @author Jozef Hartinger
 *
 */
public final class ContextualInstance {

    private ContextualInstance() {
    }

    /**
     * Shortcut for obtaining contextual instances with semantics equivalent to:
     * <code>
     * if (ctx == null) {
     *     ctx = manager.createCreationalContext(bean);
     * }
     * manager.getContext(bean.getScope()).get(bean, ctx);
     * </code>
     *
     * @param bean the given bean
     * @param manager the beanManager
     * @param ctx {@link CreationalContext} to be used for creation of a new instance - may be null
     * @return contextual instance of a given bean
     */
    public static <T> T get(Bean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx) {
        return getStrategy(bean).get(bean, manager, ctx);
    }

    /**
     * Shortcut for obtaining contextual instances with semantics equivalent to:
     * <code>
     * manager.getContext(bean.getScope()).get(bean);
     * </code>
     *
     * @param bean the given bean
     * @param manager the beanManager
     * @return contextual instance of a given bean or null if none exists
     */
    public static <T> T getIfExists(Bean<T> bean, BeanManagerImpl manager) {
        return getStrategy(bean).getIfExists(bean, manager);
    }

    public static <T> T get(RIBean<T> bean, BeanManagerImpl manager, CreationalContext<?> ctx) {
        return bean.getContextualInstanceStrategy().get(bean, manager, ctx);
    }

    public static <T> T getIfExists(RIBean<T> bean, BeanManagerImpl manager) {
        return bean.getContextualInstanceStrategy().getIfExists(bean, manager);
    }

    private static <T> ContextualInstanceStrategy<T> getStrategy(Bean<T> bean) {
        if (bean instanceof RIBean<?>) {
            return ((RIBean<T>) bean).getContextualInstanceStrategy();
        }
        return ContextualInstanceStrategy.defaultStrategy();
    }
}
