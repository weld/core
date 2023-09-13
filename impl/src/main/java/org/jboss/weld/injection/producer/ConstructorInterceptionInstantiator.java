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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.InvocationContext;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.construction.api.AroundConstructCallback;
import org.jboss.weld.construction.api.ConstructionHandle;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.interceptor.proxy.InterceptorMethodInvocation;
import org.jboss.weld.interceptor.proxy.WeldInvocationContextImpl;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Delegating {@link Instantiator} that takes care of {@link AroundConstruct} interceptor invocation.
 *
 * @author Jozef Hartinger
 *
 */
public class ConstructorInterceptionInstantiator<T> extends ForwardingInstantiator<T> {

    private final InterceptionModel model;
    private final SlimAnnotatedType<?> annotatedType;

    public ConstructorInterceptionInstantiator(Instantiator<T> delegate, InterceptionModel model, SlimAnnotatedType<?> type) {
        super(delegate);
        this.model = model;
        this.annotatedType = type;
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        if (ctx instanceof CreationalContextImpl<?>) {
            CreationalContextImpl<T> weldCtx = Reflections.cast(ctx);
            if (!weldCtx.isConstructorInterceptionSuppressed()) {
                registerAroundConstructCallback(weldCtx, manager);
            }
        }
        return delegate().newInstance(ctx, manager);
    }

    private void registerAroundConstructCallback(CreationalContextImpl<T> ctx, BeanManagerImpl manager) {
        final InterceptionContext interceptionContext = InterceptionContext.forConstructorInterception(model, ctx, manager,
                annotatedType);

        AroundConstructCallback<T> callback = new AroundConstructCallback<T>() {

            @Override
            public T aroundConstruct(final ConstructionHandle<T> handle, AnnotatedConstructor<T> constructor,
                    Object[] parameters, Map<String, Object> data) {
                /*
                 * The AroundConstruct interceptor method can access the constructed instance using InvocationContext.getTarget
                 * method after the InvocationContext.proceed completes.
                 */
                final AtomicReference<T> target = new AtomicReference<T>();

                List<InterceptorMethodInvocation> chain = interceptionContext
                        .buildInterceptorMethodInvocationsForConstructorInterception();
                InvocationContext invocationContext = new WeldInvocationContextImpl(constructor.getJavaMember(), parameters,
                        data, chain, model.getMemberInterceptorBindings(getConstructor())) {

                    @Override
                    protected Object interceptorChainCompleted() throws Exception {
                        // all the interceptors were invoked, call the constructor now
                        T instance = handle.proceed(getParameters(), getContextData());
                        target.set(instance);
                        return null;
                    }

                    @Override
                    public Object getTarget() {
                        return target.get();
                    }
                };

                try {
                    invocationContext.proceed();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new WeldException(e);
                }
                T instance = target.get();
                if (instance == null) {
                    // CDI-598
                    throw InterceptorLogger.LOG.targetInstanceNotCreated(constructor);
                }
                return instance;
            }
        };

        ctx.registerAroundConstructCallback(callback);
        ctx.setAroundConstructInterceptionContext(interceptionContext);
    }

    @Override
    public String toString() {
        return "ConstructorInterceptionInstantiator wrapping " + delegate();
    }
}
