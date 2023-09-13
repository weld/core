/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.events;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.bootstrap.BeanDeploymentFinder;
import org.jboss.weld.bootstrap.event.InterceptorConfigurator;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * @author Tomas Remes
 */
public class InterceptorConfiguratorImpl implements InterceptorConfigurator {

    private int priority = jakarta.interceptor.Interceptor.Priority.APPLICATION;

    private Set<Annotation> bindings;

    private InterceptionType type;

    private Function<InvocationContext, Object> interceptorFunction;

    private BiFunction<InvocationContext, Bean<?>, Object> interceptorBiFunction;

    private BeanManagerImpl beanManager;

    private BeanDeploymentFinder beanDeploymentFinder;

    public InterceptorConfiguratorImpl() {
        this(null);
    }

    public InterceptorConfiguratorImpl(BeanManagerImpl beanManager) {
        this.bindings = new HashSet<>();
        this.beanManager = beanManager;
    }

    @Override
    public InterceptorConfigurator intercept(InterceptionType interceptionType,
            Function<InvocationContext, Object> interceptorFunction) {
        this.type = interceptionType;
        this.interceptorFunction = interceptorFunction;
        this.interceptorBiFunction = null;
        return this;
    }

    @Override
    public InterceptorConfigurator interceptWithMetadata(InterceptionType interceptionType,
            BiFunction<InvocationContext, Bean<?>, Object> interceptorFunction) {
        this.type = interceptionType;
        this.interceptorBiFunction = interceptorFunction;
        this.interceptorFunction = null;
        return this;
    }

    @Override
    public InterceptorConfigurator addBinding(Annotation binding) {
        Collections.addAll(this.bindings, binding);
        return this;
    }

    @Override
    public InterceptorConfigurator addBindings(Annotation... bindings) {
        Collections.addAll(this.bindings, bindings);
        return this;
    }

    @Override
    public InterceptorConfigurator addBindings(Set<Annotation> bindings) {
        this.bindings.addAll(bindings);
        return this;
    }

    @Override
    public InterceptorConfigurator bindings(Annotation... bindings) {
        this.bindings.clear();
        Collections.addAll(this.bindings, bindings);
        return this;
    }

    @Override
    public InterceptorConfigurator priority(int priority) {
        this.priority = priority;
        return this;
    }

    public Interceptor<?> build() {
        BuilderInterceptorBean interceptor;
        if (type == null) {
            throw BeanLogger.LOG.noInterceptionType(this);
        }
        if (interceptorFunction == null && interceptorBiFunction == null) {
            throw BeanLogger.LOG.noInterceptionFunction(this);
        }
        if (beanDeploymentFinder != null) {
            beanManager = beanDeploymentFinder.getOrCreateBeanDeployment(BuilderInterceptorInstance.class).getBeanManager();
        }
        if (interceptorBiFunction != null) {
            interceptor = new BuilderInterceptorBean(bindings, type, priority, beanManager, interceptorBiFunction);
        } else {
            interceptor = new BuilderInterceptorBean(bindings, type, priority, beanManager, interceptorFunction);
        }
        return interceptor;
    }

    public BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    public void setBeanDeploymentFinder(BeanDeploymentFinder beanDeploymentFinder) {
        this.beanDeploymentFinder = beanDeploymentFinder;
    }

}
