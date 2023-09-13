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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.contexts.WeldCreationalContext;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author Tomas Remes
 */
class BuilderInterceptorBean implements Interceptor<BuilderInterceptorInstance>, Prioritized, PassivationCapable {

    private final Set<Annotation> bindings;

    private static final Set<Type> types = ImmutableSet.of(BuilderInterceptorInstance.class, Object.class);

    private final int priority;

    private final InterceptionType interceptionType;

    private final Function<InvocationContext, Object> interceptorFunction;

    private final BiFunction<InvocationContext, Bean<?>, Object> interceptorMetadataFunction;

    private final BeanManagerImpl beanManager;

    private BuilderInterceptorBean(Set<Annotation> interceptorBindings, InterceptionType type, int priority,
            BeanManagerImpl beanManager,
            Function<InvocationContext, Object> interceptorFunction,
            BiFunction<InvocationContext, Bean<?>, Object> interceptorMetadataFunction) {
        this.interceptorFunction = interceptorFunction;
        this.interceptorMetadataFunction = interceptorMetadataFunction;
        this.priority = priority;
        this.interceptionType = type;
        this.bindings = ImmutableSet.copyOf(interceptorBindings);
        this.beanManager = beanManager;
    }

    public BuilderInterceptorBean(Set<Annotation> interceptorBindings, InterceptionType type, int priority,
            BeanManagerImpl beanManager,
            Function<InvocationContext, Object> interceptorFunction) {
        this(interceptorBindings, type, priority, beanManager, interceptorFunction, null);
    }

    public BuilderInterceptorBean(Set<Annotation> interceptorBindings, InterceptionType type, int priority,
            BeanManagerImpl beanManager,
            BiFunction<InvocationContext, Bean<?>, Object> interceptorFunction) {
        this(interceptorBindings, type, priority, beanManager, null, interceptorFunction);
    }

    @Override
    public Set<Annotation> getInterceptorBindings() {
        return bindings;
    }

    @Override
    public boolean intercepts(InterceptionType type) {
        return interceptionType.equals(type);
    }

    @Override
    public Object intercept(InterceptionType type, BuilderInterceptorInstance builderInterceptorInstance, InvocationContext ctx)
            throws Exception {
        if (interceptorMetadataFunction != null) {
            return interceptorMetadataFunction.apply(ctx, builderInterceptorInstance.getInterceptedBean());
        } else {
            return interceptorFunction.apply(ctx);
        }
    }

    @Override
    public Class<?> getBeanClass() {
        return BuilderInterceptorInstance.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return Collections.emptySet();
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return getBeanClass().toString() + interceptionType.name() + priority + Formats.formatAnnotations(bindings);
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    protected WeldCreationalContext<?> getParentCreationalContext(CreationalContext<BuilderInterceptorInstance> ctx) {
        if (ctx instanceof WeldCreationalContext<?>) {
            WeldCreationalContext<?> parentContext = ((WeldCreationalContext<?>) ctx).getParentCreationalContext();
            if (parentContext != null) {
                return parentContext;
            }
        }
        throw BeanLogger.LOG.unableToDetermineParentCreationalContext(ctx);
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public BuilderInterceptorInstance create(CreationalContext<BuilderInterceptorInstance> creationalContext) {
        if (this.interceptorMetadataFunction != null) {
            WeldCreationalContext<?> interceptorContext = getParentCreationalContext(creationalContext);
            Contextual<?> interceptedContextual = interceptorContext.getContextual();
            if (interceptedContextual instanceof Bean<?>) {
                return new BuilderInterceptorInstance((Bean<?>) interceptedContextual, beanManager.getContextId());
            } else {
                throw BeanLogger.LOG.cannotCreateContextualInstanceOfBuilderInterceptor(this);
            }
        } else {
            return new BuilderInterceptorInstance();
        }
    }

    @Override
    public void destroy(BuilderInterceptorInstance instance, CreationalContext<BuilderInterceptorInstance> creationalContext) {
    }

    @Override
    public String getId() {
        return this.getName();
    }
}
