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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.spi.CreationalContext;
import javax.interceptor.InvocationContext;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.proxy.CombinedInterceptorAndDecoratorStackMethodHandler;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.AroundConstructCallback;
import org.jboss.weld.interceptor.proxy.DefaultInvocationContextFactory;
import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.interceptor.proxy.InterceptorInvocation;
import org.jboss.weld.interceptor.proxy.InterceptorInvocationContext;
import org.jboss.weld.interceptor.proxy.InterceptorMethodHandler;
import org.jboss.weld.interceptor.proxy.SimpleInterceptionChain;
import org.jboss.weld.interceptor.reader.TargetClassInterceptorMetadata;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.metadata.InterceptorMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.interceptor.spi.model.InterceptionType;
import org.jboss.weld.interceptor.util.InterceptionTypeRegistry;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * A wrapper over {@link SubclassedComponentInstantiator} that registers interceptors within the method handler. This class is
 * thread-safe.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class InterceptorApplyingInstantiator<T> implements Instantiator<T> {

    private final TargetClassInterceptorMetadata<T> targetClassInterceptorMetadata;
    private final InterceptionModel<ClassMetadata<?>, ?> interceptionModel;
    private final Instantiator<T> delegate;
    private final Constructor<T> constructor;

    public InterceptorApplyingInstantiator(EnhancedAnnotatedType<T> type, Instantiator<T> delegate, BeanManagerImpl manager, Constructor<T> constructor) {
        this.targetClassInterceptorMetadata = manager.getInterceptorMetadataReader().getTargetClassInterceptorMetadata(manager.getInterceptorMetadataReader().getClassMetadata(type.getJavaClass()));
        this.interceptionModel = manager.getInterceptorModelRegistry().get(type.getJavaClass());
        this.delegate = delegate;
        this.constructor = constructor;
    }

    @Override
    public T newInstance(CreationalContext<T> ctx, BeanManagerImpl manager, AroundConstructCallback<T> ignored) {
        InterceptionContext interceptionContext = new InterceptionContext(targetClassInterceptorMetadata, interceptionModel, ctx, manager);

        T instance = invokeConstructor(interceptionContext, ctx, manager);

        applyInterceptors(instance, interceptionContext);
        return instance;
    }

    protected T invokeConstructor(InterceptionContext interceptionContext, final CreationalContext<T> ctx, final BeanManagerImpl manager) {

        AroundConstructCallback<T> callback = null;

        if (InterceptionTypeRegistry.isSupported(InterceptionType.AROUND_CONSTRUCT)) {
            List<? extends InterceptorMetadata<?>> interceptors = interceptionModel.getConstructorInvocationInterceptors();
            if (!interceptors.isEmpty()) {

                // build interceptor invocations
                final Collection<InterceptorInvocation> interceptorInvocations = new ArrayList<InterceptorInvocation>(interceptors.size());
                for (InterceptorMetadata<?> interceptorMetadata : interceptors) {
                    interceptorInvocations.add(interceptorMetadata.getInterceptorInvocation(interceptionContext.getInterceptorInstance(interceptorMetadata), InterceptionType.AROUND_CONSTRUCT));
                }

                callback = new AroundConstructCallback<T>() {

                    @Override
                    public T aroundConstruct(Object[] parameters, final ConstructionHandle<T> constructionHandle) {

                        /*
                         * The AroundConstruct interceptor method can access the constructed instance using InvocationContext.getTarget
                         * method after the InvocationContext.proceed completes.
                         */
                        final AtomicReference<T> target = new AtomicReference<T>();

                        SimpleInterceptionChain chain = new SimpleInterceptionChain(interceptorInvocations) {
                            @Override
                            protected Object interceptorChainCompleted(InvocationContext invocationCtx) throws Exception {
                                // all the interceptors were invoked, call the constructor now
                                target.set(constructionHandle.construct(invocationCtx.getParameters()));
                                return null;
                            }
                        };

                        InterceptorInvocationContext invocationCtx = new InterceptorInvocationContext(chain, constructor, parameters) {
                            @Override
                            public Object getTarget() {
                                return target.get();
                            }
                        };

                        try {
                            chain.invokeNextInterceptor(invocationCtx);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Throwable e) {
                            throw new WeldException(e);
                        }
                        return target.get();
                    }
                };
            }
        }

        return delegate.newInstance(ctx, manager, callback);
    }

    protected T applyInterceptors(T instance, InterceptionContext interceptionContext) {
        try {
            InterceptorMethodHandler methodHandler = new InterceptorMethodHandler(interceptionContext, new DefaultInvocationContextFactory());
            CombinedInterceptorAndDecoratorStackMethodHandler wrapperMethodHandler = (CombinedInterceptorAndDecoratorStackMethodHandler) ((ProxyObject) instance).getHandler();
            wrapperMethodHandler.setInterceptorMethodHandler(methodHandler);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return instance;
    }



    @Override
    public String toString() {
        return "InterceptorApplyingInstantiator for " + delegate;
    }

    @Override
    public boolean hasInterceptorSupport() {
        return true;
    }

    @Override
    public boolean hasDecoratorSupport() {
        return delegate.hasDecoratorSupport();
    }
}
