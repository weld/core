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

import java.lang.reflect.Modifier;
import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedConstructor;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.bean.proxy.ProxyInstantiator;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.reflection.Formats;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class BeanInjectionTarget<T> extends BasicInjectionTarget<T> {

    public static <T> BeanInjectionTarget<T> createDefault(EnhancedAnnotatedType<T> type, Bean<T> bean,
            BeanManagerImpl beanManager) {
        return new BeanInjectionTarget<T>(type, bean, beanManager);
    }

    public static <T> BeanInjectionTarget<T> forCdiInterceptor(EnhancedAnnotatedType<T> type, Bean<T> bean,
            BeanManagerImpl manager) {
        return new BeanInjectionTarget<T>(type, bean, manager, ResourceInjector.of(type, bean, manager),
                NoopLifecycleCallbackInvoker.<T> getInstance());
    }

    private final Bean<T> bean;

    public BeanInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Injector<T> injector,
            LifecycleCallbackInvoker<T> invoker) {
        super(type, bean, beanManager, injector, invoker);
        this.bean = bean;
    }

    public BeanInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        this(type, bean, beanManager, ResourceInjector.of(type, bean, beanManager), DefaultLifecycleCallbackInvoker.of(type));
    }

    @Override
    public void dispose(T instance) {
        // No-op
    }

    protected boolean isInterceptor() {
        return (getBean() instanceof Interceptor<?>) || getType().isAnnotationPresent(jakarta.interceptor.Interceptor.class);
    }

    protected boolean isDecorator() {
        return (getBean() instanceof Decorator<?>) || getType().isAnnotationPresent(jakarta.decorator.Decorator.class);
    }

    protected boolean isInterceptionCandidate() {
        return !isInterceptor() && !isDecorator() && !Modifier.isAbstract(getType().getJavaClass().getModifiers());
    }

    protected void initializeInterceptionModel(EnhancedAnnotatedType<T> annotatedType) {
        AbstractInstantiator<T> instantiator = (AbstractInstantiator<T>) getInstantiator();
        if (instantiator.getConstructorInjectionPoint() == null) {
            return; // this is a non-producible InjectionTarget (only created to inject existing instances)
        }
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(getType())) {
            buildInterceptionModel(annotatedType, instantiator);
        }
    }

    protected void buildInterceptionModel(EnhancedAnnotatedType<T> annotatedType, AbstractInstantiator<T> instantiator) {
        new InterceptionModelInitializer<T>(beanManager, annotatedType,
                annotatedType.getDeclaredEnhancedConstructor(instantiator.getConstructorInjectionPoint().getSignature()),
                getBean()).init();
    }

    @Override
    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        initializeInterceptionModel(annotatedType);

        InterceptionModel interceptionModel = null;
        if (isInterceptionCandidate()) {
            interceptionModel = beanManager.getInterceptorModelRegistry().get(getType());
        }
        boolean hasNonConstructorInterceptors = interceptionModel != null
                && (interceptionModel.hasExternalNonConstructorInterceptors()
                        || interceptionModel.hasTargetClassInterceptors());

        List<Decorator<?>> decorators = null;
        if (getBean() != null && isInterceptionCandidate()) {
            decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        }
        boolean hasDecorators = decorators != null && !decorators.isEmpty();
        if (hasDecorators) {
            checkDecoratedMethods(annotatedType, decorators);
        }

        if (hasNonConstructorInterceptors || hasDecorators) {
            if (!(getInstantiator() instanceof DefaultInstantiator<?>)) {
                throw new java.lang.IllegalStateException("Unexpected instantiator " + getInstantiator());
            }
            DefaultInstantiator<T> delegate = (DefaultInstantiator<T>) getInstantiator();
            setInstantiator(SubclassedComponentInstantiator.forInterceptedDecoratedBean(annotatedType, getBean(), delegate,
                    beanManager));
            if (hasDecorators) {
                setInstantiator(new SubclassDecoratorApplyingInstantiator<T>(getBeanManager().getContextId(), getInstantiator(),
                        getBean(), decorators));
            }
            if (hasNonConstructorInterceptors) {
                setInstantiator(new InterceptorApplyingInstantiator<T>(getInstantiator(), interceptionModel, getType()));
            }
        }

        if (isInterceptionCandidate()) {
            setupConstructorInterceptionInstantiator(interceptionModel);
        }
    }

    protected void setupConstructorInterceptionInstantiator(InterceptionModel interceptionModel) {
        if (interceptionModel != null && interceptionModel.hasExternalConstructorInterceptors()) {
            setInstantiator(new ConstructorInterceptionInstantiator<T>(getInstantiator(), interceptionModel, getType()));
        }
    }

    protected void checkNoArgsConstructor(EnhancedAnnotatedType<T> type) {
        EnhancedAnnotatedConstructor<T> constructor = type.getNoArgsEnhancedConstructor();
        if (constructor == null) {
            if (!beanManager.getServices().get(ProxyInstantiator.class).isUsingConstructor()) {
                // Relaxed construction allows us to bypass non-existent no-arg constructor
                return;
            } else {
                // Without relaxed contruction, CDI enforces existence of no-arg constructor
                throw BeanLogger.LOG.decoratedHasNoNoargsConstructor(this);
            }
        } else {
            if (constructor.isPrivate()) {
                // Private no-arg constructor is a problem while creating the decorator and we have to blow up
                throw BeanLogger.LOG.decoratedNoargsConstructorIsPrivate(this,
                        Formats.formatAsStackTraceElement(constructor.getJavaMember()));
            }
        }
    }

    protected void checkDecoratedMethods(EnhancedAnnotatedType<T> type, List<Decorator<?>> decorators) {
        if (type.isFinal()) {
            throw BeanLogger.LOG.finalBeanClassWithDecoratorsNotAllowed(this);
        }
        checkNoArgsConstructor(type);
        for (Decorator<?> decorator : decorators) {
            EnhancedAnnotatedType<?> decoratorClass;
            if (decorator instanceof DecoratorImpl<?>) {
                DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
                decoratorClass = decoratorBean.getBeanManager().getServices().get(ClassTransformer.class)
                        .getEnhancedAnnotatedType(decoratorBean.getAnnotated());
            } else if (decorator instanceof CustomDecoratorWrapper<?>) {
                decoratorClass = ((CustomDecoratorWrapper<?>) decorator).getEnhancedAnnotated();
            } else {
                throw BeanLogger.LOG.nonContainerDecorator(decorator);
            }

            for (EnhancedAnnotatedMethod<?, ?> decoratorMethod : decoratorClass.getEnhancedMethods()) {
                EnhancedAnnotatedMethod<?, ?> method = type.getEnhancedMethod(decoratorMethod.getSignature());
                if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal()) {
                    throw BeanLogger.LOG.finalBeanClassWithInterceptorsNotAllowed(this);
                }
            }
        }
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        T instance = super.produce(ctx);
        if (bean != null && !bean.getScope().equals(Dependent.class) && !getInstantiator().hasDecoratorSupport()) {
            // This should be safe, but needs verification PLM
            // Without this, the chaining of decorators will fail as the
            // incomplete instance will be resolved
            ctx.push(instance);
        }
        return instance;
    }

    @Override
    public Bean<T> getBean() {
        return bean;
    }
}
