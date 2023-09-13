/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.Interceptors;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.util.collections.ImmutableSet;
import org.jboss.weld.util.reflection.Reflections;

/**
 * Basic {@link InjectionTarget} implementation. The implementation supports:
 * <ul>
 * <li>@Inject injection + initializers</li>
 * <li>@PostConstruct/@PreDestroy callbacks</li>
 * </ul>
 *
 * Interception and decoration is not supported but can be added using extension points.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class BasicInjectionTarget<T> extends AbstractProducer<T> implements WeldInjectionTarget<T> {

    public static <T> BasicInjectionTarget<T> create(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Injector<T> injector, LifecycleCallbackInvoker<T> invoker) {
        return new BasicInjectionTarget<T>(type, bean, beanManager, injector, invoker);
    }

    public static <T> BasicInjectionTarget<T> createDefault(EnhancedAnnotatedType<T> type, Bean<T> bean,
            BeanManagerImpl beanManager, Instantiator<T> instantiator) {
        return new BasicInjectionTarget<T>(type, bean, beanManager, instantiator);
    }

    /**
     * Creates {@link InjectionTarget} for interceptors which do not have associated {@link Interceptor}. These interceptors are
     * a
     * result of using {@link Interceptors} annotation directly on the target class.
     */
    public static <T> BasicInjectionTarget<T> createNonCdiInterceptor(EnhancedAnnotatedType<T> type,
            BeanManagerImpl beanManager) {
        return new BasicInjectionTarget<T>(type, null, beanManager, DefaultInjector.of(type, null, beanManager),
                NoopLifecycleCallbackInvoker.<T> getInstance());
    }

    protected final BeanManagerImpl beanManager;
    private final SlimAnnotatedType<T> type;
    private final Set<InjectionPoint> injectionPoints;

    // Instantiation
    private Instantiator<T> instantiator;
    private final Injector<T> injector;
    private final LifecycleCallbackInvoker<T> invoker;

    protected BasicInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Injector<T> injector, LifecycleCallbackInvoker<T> invoker) {
        this(type, bean, beanManager, injector, invoker, null);
    }

    protected BasicInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Injector<T> injector, LifecycleCallbackInvoker<T> invoker, Instantiator<T> instantiator) {
        this.beanManager = beanManager;
        this.type = type.slim();
        this.injector = injector;
        this.invoker = invoker;

        final Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();

        checkType(type);
        this.injector.registerInjectionPoints(injectionPoints);
        if (instantiator != null) {
            this.instantiator = instantiator;
        } else {
            this.instantiator = initInstantiator(type, bean, beanManager, injectionPoints);
        }
        this.injectionPoints = ImmutableSet.copyOf(injectionPoints);
        checkDelegateInjectionPoints();
    }

    protected BasicInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Instantiator<T> instantiator) {
        this(type, bean, beanManager, DefaultInjector.of(type, bean, beanManager), DefaultLifecycleCallbackInvoker.of(type),
                instantiator);
    }

    protected void checkType(EnhancedAnnotatedType<T> type) {
        if (!Reflections.isTopLevelOrStaticNestedClass(type.getJavaClass())) {
            throw BeanLogger.LOG.simpleBeanAsNonStaticInnerClassNotAllowed(type);
        }
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        return instantiator.newInstance(ctx, beanManager);
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        injector.inject(instance, ctx, beanManager, type, this);
    }

    @Override
    public void postConstruct(T instance) {
        invoker.postConstruct(instance, instantiator);
    }

    @Override
    public void preDestroy(T instance) {
        invoker.preDestroy(instance, instantiator);
    }

    @Override
    public void dispose(T instance) {
        // No-op
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    protected SlimAnnotatedType<T> getType() {
        return type;
    }

    public BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    public Instantiator<T> getInstantiator() {
        return instantiator;
    }

    public void setInstantiator(Instantiator<T> instantiator) {
        this.instantiator = instantiator;
    }

    public boolean hasInterceptors() {
        return instantiator.hasInterceptorSupport();
    }

    public boolean hasDecorators() {
        return instantiator.hasDecoratorSupport();
    }

    protected void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
    }

    /**
     * Returns an instantiator that will be used to create a new instance of a given component. If the instantiator uses a
     * constructor with injection points, the implementation of the
     * {@link #initInstantiator(EnhancedAnnotatedType, Bean, BeanManagerImpl, Set)} method is supposed to register all these
     * injection points within the injectionPoints set passed in as a parameter.
     */
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager,
            Set<InjectionPoint> injectionPoints) {
        DefaultInstantiator<T> instantiator = new DefaultInstantiator<T>(type, bean, beanManager);
        injectionPoints.addAll(instantiator.getParameterInjectionPoints());
        return instantiator;
    }

    @Override
    public AnnotatedType<T> getAnnotated() {
        return type;
    }

    @Override
    public AnnotatedType<T> getAnnotatedType() {
        return getAnnotated();
    }

    public Injector<T> getInjector() {
        return injector;
    }

    public LifecycleCallbackInvoker<T> getLifecycleCallbackInvoker() {
        return invoker;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("InjectionTarget for ");
        if (getBean() == null) {
            result.append(getAnnotated());
        } else {
            result.append(getBean());
        }
        return result.toString();
    }

    @Override
    public Bean<T> getBean() {
        return null;
    }
}
