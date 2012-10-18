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

import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED;
import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;
import static org.jboss.weld.logging.messages.BeanMessage.NON_CONTAINER_DECORATOR;
import static org.jboss.weld.logging.messages.BeanMessage.SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedMethod;
import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.DecoratorImpl;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.util.BeanMethods;
import org.jboss.weld.util.InjectionPoints;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public abstract class AbstractInjectionTarget<T> extends AbstractProducer<T> implements InjectionTarget<T> {

    protected final BeanManagerImpl beanManager;
    private final AnnotatedType<T> type;
    private final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
    private final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;
    private final List<AnnotatedMethod<? super T>> postConstructMethods;
    private final List<AnnotatedMethod<? super T>> preDestroyMethods;
    private final Set<InjectionPoint> injectionPoints;
    private final Bean<T> bean;

    // Instantiation
    private Instantiator<T> instantiator;

    public AbstractInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.type = type.slim();
        Set<InjectionPoint> injectionPoints = new HashSet<InjectionPoint>();

        this.bean = bean;
        this.injectableFields = InjectionPointFactory.instance().getFieldInjectionPoints(bean, type, beanManager);
        injectionPoints.addAll(InjectionPoints.flattenInjectionPoints(this.injectableFields));
        this.initializerMethods = BeanMethods.getInitializerMethods(bean, type, beanManager);
        injectionPoints.addAll(InjectionPoints.flattenParameterInjectionPoints(initializerMethods));
        if (isInterceptor()) {
            this.postConstructMethods = Collections.emptyList();
            this.preDestroyMethods = Collections.emptyList();
        } else {
            this.postConstructMethods = BeanMethods.getPostConstructMethods(type);
            this.preDestroyMethods = BeanMethods.getPreDestroyMethods(type);
        }

        checkType(type);
        this.instantiator = initInstantiator(type, bean, beanManager, injectionPoints);
        this.injectionPoints = WeldCollections.immutableSet(injectionPoints);
        checkDelegateInjectionPoints();
    }

    protected void checkType(EnhancedAnnotatedType<T> type) {
        if (type.isAnonymousClass() || (type.isMemberClass() && !type.isStatic())) {
            throw new DefinitionException(SIMPLE_BEAN_AS_NON_STATIC_INNER_CLASS_NOT_ALLOWED, type);
        }
    }

    protected void checkDecoratedMethods(EnhancedAnnotatedType<T> type, List<Decorator<?>> decorators) {
        if (type.isFinal()) {
            throw new DeploymentException(FINAL_BEAN_CLASS_WITH_DECORATORS_NOT_ALLOWED, this);
        }
        for (Decorator<?> decorator : decorators) {
            AnnotatedType<?> decoratorClass;
            if (decorator instanceof DecoratorImpl<?>) {
                DecoratorImpl<?> decoratorBean = (DecoratorImpl<?>) decorator;
                decoratorClass = decoratorBean.getAnnotated();
            } else if (decorator instanceof CustomDecoratorWrapper<?>) {
                decoratorClass = ((CustomDecoratorWrapper<?>) decorator).getEnhancedAnnotated();
            } else {
                throw new IllegalStateException(NON_CONTAINER_DECORATOR, decorator);
            }

            EnhancedAnnotatedType<?> enhancedDecoratorClass = beanManager.getServices().get(ClassTransformer.class).getEnhancedAnnotatedType(decoratorClass);
            for (EnhancedAnnotatedMethod<?, ?> decoratorMethod : enhancedDecoratorClass.getEnhancedMethods()) {
                EnhancedAnnotatedMethod<?, ?> method = type.getEnhancedMethod(decoratorMethod.getSignature());
                if (method != null && !method.isStatic() && !method.isPrivate() && method.isFinal()) {
                    throw new DeploymentException(FINAL_BEAN_CLASS_WITH_INTERCEPTORS_NOT_ALLOWED, method, decoratorMethod);
                }
            }
        }
    }

    protected boolean isInterceptor() {
        return (bean instanceof Interceptor<?>) || type.isAnnotationPresent(javax.interceptor.Interceptor.class);
    }

    protected boolean isDecorator() {
        return (bean instanceof Decorator<?>) || type.isAnnotationPresent(javax.decorator.Decorator.class);
    }

    protected boolean isInterceptionCandidate() {
        return !isInterceptor() && !isDecorator();
    }

    public T produce(CreationalContext<T> ctx) {
        T instance = instantiator.newInstance(ctx, beanManager);
        if (bean != null && !bean.getScope().equals(Dependent.class) && !instantiator.hasDecoratorSupport()) {
            // This should be safe, but needs verification PLM
            // Without this, the chaining of decorators will fail as the
            // incomplete instance will be resolved
            ctx.push(instance);
        }
        return instance;
    }

    public void postConstruct(T instance) {
        for (AnnotatedMethod<? super T> method : postConstructMethods) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    RuntimeAnnotatedMembers.invokeMethod(method, instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
    }

    public void preDestroy(T instance) {
        for (AnnotatedMethod<? super T> method : preDestroyMethods) {
            if (method != null) {
                try {
                    // note: RI supports injection into @PreDestroy
                    RuntimeAnnotatedMembers.invokeMethod(method, instance);
                } catch (Exception e) {
                    throw new WeldException(INVOCATION_ERROR, e, method, instance);
                }
            }
        }
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

    public List<Set<FieldInjectionPoint<?, ?>>> getInjectableFields() {
        return injectableFields;
    }

    public List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods() {
        return initializerMethods;
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

    public List<AnnotatedMethod<? super T>> getPostConstructMethods() {
        return postConstructMethods;
    }

    public List<AnnotatedMethod<? super T>> getPreDestroyMethods() {
        return preDestroyMethods;
    }

    protected void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(annotatedType.getJavaClass())) {
            new InterceptionModelInitializer<T>(beanManager, annotatedType, getBean()).init();
        }
    }

    /**
     * Returns an instantiator that will be used to create a new instance of a given component. If the instantiator uses a
     * constructor with injection points, the implementation of the
     * {@link #initInstantiator(EnhancedAnnotatedType, Bean, BeanManagerImpl, Set)} method is supposed to register all these
     * injection points within the injectionPoints set passed in as a parameter.
     */
    protected abstract Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints);

    @Override
    public AnnotatedType<T> getAnnotated() {
        return type;
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
