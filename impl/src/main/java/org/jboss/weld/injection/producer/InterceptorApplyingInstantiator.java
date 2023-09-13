/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import jakarta.enterprise.context.spi.CreationalContext;

import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.contexts.CreationalContextImpl;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.interceptor.proxy.InterceptorMethodHandler;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.reflection.Reflections;

/**
 * A wrapper over {@link SubclassedComponentInstantiator} that registers interceptors within the method handler. This class is
 * thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InterceptorApplyingInstantiator<T> extends ForwardingInstantiator<T> {

    private final InterceptionModel interceptionModel;
    private final SlimAnnotatedType<T> annotatedType;

    public InterceptorApplyingInstantiator(Instantiator<T> delegate, InterceptionModel model, SlimAnnotatedType<T> type) {
        super(delegate);
        this.interceptionModel = model;
        this.annotatedType = type;
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager) {
        InterceptionContext interceptionContext = null;
        if (ctx instanceof CreationalContextImpl<?>) {
            CreationalContextImpl<T> weldCtx = Reflections.cast(ctx);
            interceptionContext = weldCtx.getAroundConstructInterceptionContext();
        }
        if (interceptionContext == null) {
            // There is no interception context to reuse
            interceptionContext = InterceptionContext.forNonConstructorInterception(interceptionModel, ctx, manager,
                    annotatedType);
        }
        T instance = delegate().newInstance(ctx, manager);
        applyInterceptors(instance, interceptionContext);
        return instance;
    }

    protected T applyInterceptors(T instance, InterceptionContext interceptionContext) {
        try {
            InterceptorMethodHandler methodHandler = new InterceptorMethodHandler(interceptionContext);
            CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) ((ProxyObject) instance)
                    .weld_getHandler();
            wrapperMethodHandler.setInterceptorMethodHandler(methodHandler);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return instance;
    }

    @Override
    public String toString() {
        return "InterceptorApplyingInstantiator for " + delegate();
    }

    @Override
    public boolean hasInterceptorSupport() {
        return true;
    }
}
