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
package org.jboss.weld.injection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InterceptionFactory;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import org.jboss.weld.bean.proxy.InterceptedProxyMethodHandler;
import org.jboss.weld.bean.proxy.InterceptionFactoryDataCache;
import org.jboss.weld.bean.proxy.InterceptionFactoryDataCache.InterceptionFactoryData;
import org.jboss.weld.bean.proxy.ProxyObject;
import org.jboss.weld.bootstrap.events.configurator.AnnotatedTypeConfiguratorImpl;
import org.jboss.weld.exceptions.UnproxyableResolutionException;
import org.jboss.weld.interceptor.proxy.InterceptionContext;
import org.jboss.weld.interceptor.proxy.InterceptorMethodHandler;
import org.jboss.weld.logging.InterceptorLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Proxies;

/**
 * Instances of this class are not suitable for sharing between threads.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class InterceptionFactoryImpl<T> implements InterceptionFactory<T> {

    /**
     *
     * @param beanManager
     * @param creationalContext
     * @param annotatedType
     * @return
     */
    public static <F> InterceptionFactoryImpl<F> of(BeanManagerImpl beanManager, CreationalContext<?> creationalContext, AnnotatedType<F> annotatedType) {
        return new InterceptionFactoryImpl<>(beanManager, creationalContext, annotatedType);
    }

    private final BeanManagerImpl beanManager;

    private final CreationalContext<?> creationalContext;

    private final AnnotatedType<T> annotatedType;

    private AnnotatedTypeConfiguratorImpl<T> configurator;

    private boolean ignoreFinalMethods;

    private boolean used;

    private InterceptionFactoryImpl(BeanManagerImpl beanManager, CreationalContext<?> creationalContext, AnnotatedType<T> annotatedType) {
        this.beanManager = beanManager;
        this.creationalContext = creationalContext;
        this.annotatedType = annotatedType;
        this.ignoreFinalMethods = false;
        this.used = false;
    }

    @Override
    public InterceptionFactory<T> ignoreFinalMethods() {
        InterceptorLogger.LOG.interceptionFactoryIgnoreFinalMethodsInvoked(annotatedType.getJavaClass().getSimpleName());
        // Note that final methods are always ignored during proxy generation
        ignoreFinalMethods = true;
        return this;
    }

    @Override
    public AnnotatedTypeConfigurator<T> configure() {
        InterceptorLogger.LOG.interceptionFactoryConfigureInvoked(annotatedType.getJavaClass().getSimpleName());
        if (configurator == null) {
            configurator = new AnnotatedTypeConfiguratorImpl<>(annotatedType);
        }
        return configurator;
    }

    @Override
    public T createInterceptedInstance(T instance) {

        if (used) {
            throw InterceptorLogger.LOG.interceptionFactoryNotReusable();
        }

        if (instance instanceof ProxyObject) {
            InterceptorLogger.LOG.interceptionFactoryInternalContainerConstruct(instance.getClass());
            return instance;
        }

        UnproxyableResolutionException exception = Proxies.getUnproxyableTypeException(annotatedType.getBaseType(), null, beanManager.getServices(),
            ignoreFinalMethods);
        if (exception != null) {
            throw exception;
        }
        used = true;

        AnnotatedType<T> originalAT = configurator != null ? configurator.complete() : annotatedType;
        // this is the resulting AT we want to work with
        AnnotatedType<T> resultingAnnotatedType = null;
        // if it is an interface, we will have to 'merge' the AT with the AT of whatever user supplied as an instance
        if (originalAT.getJavaClass().isInterface()) {
            AnnotatedType<?> instanceAnnotatedType = beanManager.createAnnotatedType(instance.getClass());
            AnnotatedTypeConfiguratorImpl<?> newAtConfigurator = new AnnotatedTypeConfiguratorImpl<>(instanceAnnotatedType);

            // first check class-level annotation
            for (Annotation annotation : originalAT.getAnnotations()) {
                if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                    if (newAtConfigurator.getAnnotated().isAnnotationPresent(annotation.annotationType())) {
                        // same annotation is already present on the type, throw an exception
                        throw InterceptorLogger.LOG.interceptionFactoryAnnotationClash(originalAT, newAtConfigurator.getAnnotated(), annotation);
                    }
                    newAtConfigurator.add(annotation);
                }
            }
            // then we go over methods
            for (AnnotatedMethod<? super T> annMethod : originalAT.getMethods()) {
                for (Annotation annotation : annMethod.getAnnotations()) {
                    if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                        newAtConfigurator.filterMethods((m) ->
                            // cannot use equals on Method since they are not declared on the same class
                            isMatchingMethod(annMethod.getJavaMember(), m.getJavaMember(), instance.getClass()))
                            .findFirst().ifPresent(result -> {
                                if (result.getAnnotated().isAnnotationPresent(annotation.annotationType())) {
                                    // same annotation is already present on the type, throw an exception
                                    throw InterceptorLogger.LOG.interceptionFactoryAnnotationClash(originalAT, newAtConfigurator.getAnnotated(), annotation);
                                }
                                result.add(annotation);
                            });
                    }
                }
            }
            // cast should be safe as provided instance was a subclass of the interface
            resultingAnnotatedType = (AnnotatedType<T>) newAtConfigurator.complete();
        } else {
            resultingAnnotatedType = originalAT;
        }
        Optional<InterceptionFactoryData<T>> cached = beanManager.getServices().get(InterceptionFactoryDataCache.class)
            .getInterceptionFactoryData(resultingAnnotatedType);

        if (!cached.isPresent()) {
            InterceptorLogger.LOG.interceptionFactoryNotRequired(resultingAnnotatedType.getJavaClass().getSimpleName());
            return instance;
        }

        InterceptionFactoryData<T> data = cached.get();

        InterceptedProxyMethodHandler methodHandler = new InterceptedProxyMethodHandler(instance);
        methodHandler.setInterceptorMethodHandler(new InterceptorMethodHandler(
            InterceptionContext.forNonConstructorInterception(data.getInterceptionModel(), creationalContext, beanManager, data.getSlimAnnotatedType())));

        T proxy = (System.getSecurityManager() == null) ? data.getInterceptedProxyFactory().run()
            : AccessController.doPrivileged(data.getInterceptedProxyFactory());
        ((ProxyObject) proxy).setHandler(methodHandler);

        return proxy;
    }

    private boolean isMatchingMethod(Method originalAt, Method newAt, Class<?> instanceClass) {
        if (originalAt == null || newAt == null) {
            return false;
        }
        // declaring class check with exception for default methods
        if (!originalAt.isDefault() && !newAt.getDeclaringClass().equals(instanceClass)) {
            return false;
        }
        // with generics in place, we might bump into bridge methods
        // interfaces (original AT) cannot have then, hence return false
        if (newAt.isBridge()) {
            return false;
        }
        if (!originalAt.getName().equals(newAt.getName())
            || !originalAt.getReturnType().equals(newAt.getReturnType())
            || !(originalAt.getParameterCount() == newAt.getParameterCount())) {
            return false;
        }
        List<Class<?>> originalMethodParams = Arrays.asList(originalAt.getParameterTypes());
        List<Class<?>> newMethodParams = Arrays.asList(newAt.getParameterTypes());
        for (int i = 0; i < originalMethodParams.size(); i++) {
            // there is a possible generics hell when comparing method params
            // best-effort approach is to check assignability
            if (!originalMethodParams.get(i).isAssignableFrom(newMethodParams.get(i))) {
                return false;
            }
        }
        return true;
    }
}
