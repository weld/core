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

import static org.jboss.weld.logging.messages.BeanMessage.INVOCATION_ERROR;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.Interceptor;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.annotated.runtime.RuntimeAnnotatedMembers;
import org.jboss.weld.exceptions.DefinitionException;
import org.jboss.weld.exceptions.WeldException;
import org.jboss.weld.injection.FieldInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.MethodInjectionPoint;
import org.jboss.weld.injection.WeldInjectionPoint;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.logging.messages.BeanMessage;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.InjectionPoints;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class WeldInjectionTarget<T> implements InjectionTarget<T> {

    private final BeanManagerImpl beanManager;
    private final AnnotatedType<T> type;
    private final List<Set<FieldInjectionPoint<?, ?>>> injectableFields;
    private final List<Set<MethodInjectionPoint<?, ?>>> initializerMethods;
    private final List<AnnotatedMethod<? super T>> postConstructMethods;
    private final List<AnnotatedMethod<? super T>> preDestroyMethods;
    private final Set<InjectionPoint> injectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> ejbInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> persistenceContextInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> persistenceUnitInjectionPoints;
    private final Set<WeldInjectionPoint<?, ?>> resourceInjectionPoints;
    private final Bean<T> bean;

    // Instantiation
    private Instantiator<T> instantiator;

    public WeldInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        this.beanManager = beanManager;
        this.type = type.slim();
        this.injectionPoints = new HashSet<InjectionPoint>();
        if (type.getJavaClass().isInterface()) {
            throw new DefinitionException(BeanMessage.INJECTION_TARGET_CANNOT_BE_CREATED_FOR_INTERFACE, type);
        }

        SimpleInstantiator<T> instantiator = new SimpleInstantiator<T>(type, bean, beanManager);
        injectionPoints.addAll(instantiator.getConstructor().getParameterInjectionPoints());
        this.instantiator = instantiator;

        this.injectableFields = InjectionPointFactory.instance().getFieldInjectionPoints(bean, type, beanManager);
        this.injectionPoints.addAll(InjectionPoints.flattenInjectionPoints(this.injectableFields));
        this.initializerMethods = Beans.getInitializerMethods(bean, type, beanManager);
        this.injectionPoints.addAll(InjectionPoints.flattenParameterInjectionPoints(initializerMethods));
        this.postConstructMethods = Beans.getPostConstructMethods(type);
        this.preDestroyMethods = Beans.getPreDestroyMethods(type);
        this.ejbInjectionPoints = InjectionPointFactory.instance().getEjbInjectionPoints(bean, type, beanManager);
        this.persistenceContextInjectionPoints = InjectionPointFactory.instance().getPersistenceContextInjectionPoints(bean, type, beanManager);
        this.persistenceUnitInjectionPoints = InjectionPointFactory.instance().getPersistenceUnitInjectionPoints(bean, type, beanManager);
        this.resourceInjectionPoints = InjectionPointFactory.instance().getResourceInjectionPoints(bean, type, beanManager);
        this.bean = bean;
    }

    public T produce(CreationalContext<T> ctx) {
        return instantiator.newInstance(ctx, beanManager);
    }

    public void inject(final T instance, final CreationalContext<T> ctx) {
        new InjectionContextImpl<T>(beanManager, this, getType(), instance) {
            public void proceed() {
                Beans.injectEEFields(instance, beanManager, ejbInjectionPoints, persistenceContextInjectionPoints, persistenceUnitInjectionPoints, resourceInjectionPoints);
                Beans.injectFieldsAndInitializers(instance, ctx, beanManager, injectableFields, initializerMethods);
            }

        }.run();
    }

    public void postConstruct(T instance) {
        if (instantiator.hasInterceptors()) {
            InterceptionUtils.executePostConstruct(instance);
        } else {
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
    }

    public void preDestroy(T instance) {
        if (instantiator.hasInterceptors()) {
            InterceptionUtils.executePredestroy(instance);
        } else {
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
    }

    public void dispose(T instance) {
        // No-op
    }

    public synchronized void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        if (isInterceptionCandidate() && !beanManager.getInterceptorModelRegistry().containsKey(annotatedType.getJavaClass())) {
            new InterceptionModelInitializer<T>(beanManager, annotatedType, bean).init();
        }
        boolean hasInterceptors = this.isInterceptionCandidate() && (beanManager.getInterceptorModelRegistry().containsKey(type.getJavaClass()));

        List<Decorator<?>> decorators = null;
        if (bean != null) {
            decorators = beanManager.resolveDecorators(bean.getTypes(), bean.getQualifiers());
        }
        boolean hasDecorators = decorators != null && !decorators.isEmpty();

        if (hasInterceptors || hasDecorators) {
            if (instantiator instanceof SimpleInstantiator<?>) {
                this.instantiator = new SubclassedComponentInstantiator<T>(annotatedType, bean, (SimpleInstantiator<T>) instantiator, beanManager);
            }
            if (hasDecorators) {
                this.instantiator = new EnhancedSubclassDecoratorApplyingInstantiator<T>(instantiator, bean, decorators);
            }
            if (hasInterceptors) {
                this.instantiator = new InterceptorApplyingInstantiator<T>(annotatedType, this.instantiator, beanManager);
            }
        }
    }

    protected boolean isInterceptionCandidate() {
        return !((bean instanceof Interceptor<?>) ||
           (bean instanceof Decorator<?>) ||
           type.isAnnotationPresent(javax.interceptor.Interceptor.class) ||
           type.isAnnotationPresent(javax.decorator.Decorator.class));
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

    protected List<Set<FieldInjectionPoint<?, ?>>> getInjectableFields() {
        return injectableFields;
    }

    protected List<Set<MethodInjectionPoint<?, ?>>> getInitializerMethods() {
        return initializerMethods;
    }

    public Instantiator<T> getInstantiator() {
        return instantiator;
    }

    public boolean hasInterceptors() {
        return instantiator.hasInterceptors();
    }

    public boolean hasDecorators() {
        return instantiator.hasDecorators();
    }

    @Override
    public String toString() {
        return "WeldInjectionTarget for " + type.getJavaClass();
    }
}
