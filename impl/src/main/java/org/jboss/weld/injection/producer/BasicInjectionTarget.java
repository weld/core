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

import static org.jboss.weld.logging.messages.BeanMessage.SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.slim.SlimAnnotatedType;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * Basic {@link InjectionTarget} implementation. The implementation supports:
 * <ul>
 *  <li>@Inject injection + initializers</li>
 *  <li>@PostConstruct/@PreDestroy callbacks</li>
 * </ul>
 *
 * Interception and decoration is not supported but can be added using extension points.
 *
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class BasicInjectionTarget<T> extends AbstractProducer<T> implements InjectionTarget<T> {

    protected final BeanManagerImpl beanManager;
    private final SlimAnnotatedType<T> type;
    private final Set<InjectionPoint> injectionPoints;
    private final Bean<T> bean;

    // Instantiation
    private Instantiator<T> instantiator;
    private final Injector<T> injector;
    private final LifecycleCallbackInvoker<T> invoker;

    public BasicInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.type = type.slim();
        Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();

        this.bean = bean;

        checkType(type);
        this.injector = initInjector(type, bean, beanManager);
        this.injector.registerInjectionPoints(injectionPoints);
        this.instantiator = initInstantiator(type, bean, beanManager, injectionPoints);
        this.injectionPoints = WeldCollections.immutableSet(injectionPoints);
        checkDelegateInjectionPoints();

        this.invoker = initInvoker(type);
    }

    protected void checkType(EnhancedAnnotatedType<T> type) {
        if (type.isAnonymousClass() || (type.isMemberClass() && !type.isStatic())) {
            throw new DefinitionException(SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED, type);
        }
    }

    public T produce(CreationalContext<T> ctx) {
        T instance = instantiator.newInstance(ctx, beanManager, null);
        if (bean != null && !bean.getScope().equals(Dependent.class) && !instantiator.hasDecoratorSupport()) {
            // This should be safe, but needs verification PLM
            // Without this, the chaining of decorators will fail as the
            // incomplete instance will be resolved
            ctx.push(instance);
        }
        return instance;
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        injector.inject(instance, ctx, beanManager, type, this);
    }

    public void postConstruct(T instance) {
        invoker.postConstruct(instance, instantiator);
    }

    public void preDestroy(T instance) {
        invoker.preDestroy(instance, instantiator);
    }

    public void dispose(T instance) {
        // No-op
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return injectionPoints;
    }

    protected AnnotatedType<T> getType() {
        return type;
    }

    protected BeanManagerImpl getBeanManager() {
        return beanManager;
    }

    public Instantiator<T> getInstantiator() {
        return instantiator;
    }

    public void setInstantiator(Instantiator<T> instantiator) {
        this.instantiator = instantiator;
    }

    public Bean<T> getBean() {
        return bean;
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
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints) {
        DefaultInstantiator<T> instantiator = new DefaultInstantiator<T>(type, bean, beanManager);
        injectionPoints.addAll(instantiator.getConstructor().getParameterInjectionPoints());
        return instantiator;
    }

    protected Injector<T> initInjector(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        return new DefaultInjector<T>(type, bean, beanManager);
    }

    protected LifecycleCallbackInvoker<T> initInvoker(EnhancedAnnotatedType<T> type) {
        return new DefaultLifecycleCallbackInvoker<T>(type);
    }

    @Override
    public AnnotatedType<T> getAnnotated() {
        return type;
    }

    public Injector<T> getInjector() {
        return injector;
    }

    public LifecycleCallbackInvoker<T> getLifecycleCallbackInvoker() {
        return invoker;
    }

    @Override
    public String toString() {
        if (getBean() == null) {
             return "InjectionTarget for " + getAnnotated();
        } else {
            return "InjectionTarget for " + getBean();
        }
    }
}
