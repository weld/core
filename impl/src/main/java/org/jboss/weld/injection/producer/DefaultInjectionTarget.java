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

import static org.jboss.weld.util.collections.WeldCollections.immutableSet;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.ResourceInjectionPoint;
import org.jboss.weld.interceptor.util.InterceptionUtils;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.ArraySet;

/**
 * @author Pete Muir
 * @author Jozef Hartinger
 */
public class DefaultInjectionTarget<T> extends AbstractInjectionTarget<T> {

    private final Set<ResourceInjectionPoint<?, ?>> resourceInjectionPoints;

    public DefaultInjectionTarget(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        Set<ResourceInjectionPoint<?, ?>> resourceInjectionPoints = new ArraySet<ResourceInjectionPoint<?,?>>();
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getEjbInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getPersistenceContextInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getPersistenceUnitInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getResourceInjectionPoints(bean, type, beanManager));
        resourceInjectionPoints.addAll(InjectionPointFactory.silentInstance().getWebServiceRefInjectionPoints(bean, type, beanManager));
        this.resourceInjectionPoints = immutableSet(resourceInjectionPoints);
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints) {
        DefaultInstantiator<T> instantiator = new DefaultInstantiator<T>(type, bean, beanManager);
        injectionPoints.addAll(instantiator.getConstructor().getParameterInjectionPoints());
        return instantiator;
    }

    public void inject(final T instance, final CreationalContext<T> ctx) {
        new InjectionContextImpl<T>(beanManager, this, getType(), instance) {
            public void proceed() {
                Beans.injectEEFields(resourceInjectionPoints, instance, ctx);
                Beans.injectFieldsAndInitializers(instance, ctx, beanManager, getInjectableFields(), getInitializerMethods());
            }

        }.run();
    }

    public void postConstruct(T instance) {
        if (getInstantiator().hasInterceptorSupport()) {
            InterceptionUtils.executePostConstruct(instance);
        } else {
            super.postConstruct(instance);
        }
    }

    public void preDestroy(T instance) {
        if (getInstantiator().hasInterceptorSupport()) {
            InterceptionUtils.executePredestroy(instance);
        } else {
            super.preDestroy(instance);
        }
    }

    public void dispose(T instance) {
        // No-op
    }

    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        super.initializeAfterBeanDiscovery(annotatedType);
        boolean hasInterceptors = isInterceptionCandidate() && (beanManager.getInterceptorModelRegistry().containsKey(getType().getJavaClass()));

        List<Decorator<?>> decorators = null;
        if (getBean() != null && isInterceptionCandidate()) {
            decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        }
        boolean hasDecorators = decorators != null && !decorators.isEmpty();
        if (hasDecorators) {
            checkDecoratedMethods(annotatedType, decorators);
        }

        if (hasInterceptors || hasDecorators) {
            if (!(getInstantiator() instanceof DefaultInstantiator<?>)) {
                throw new IllegalStateException("Unexpected instantiator " + getInstantiator());
            }
            DefaultInstantiator<T> delegate = (DefaultInstantiator<T>) getInstantiator();
            setInstantiator(new SubclassedComponentInstantiator<T>(annotatedType, getBean(), delegate, beanManager));

            if (hasDecorators) {
                setInstantiator(new SubclassDecoratorApplyingInstantiator<T>(getInstantiator(), getBean(), decorators));
            }
            if (hasInterceptors) {
                setInstantiator(new InterceptorApplyingInstantiator<T>(annotatedType, this.getInstantiator(), beanManager, delegate.getConstructor().getAnnotated().getJavaMember()));
            }
        }
    }

    public Set<ResourceInjectionPoint<?, ?>> getResourceInjectionPoints() {
        return resourceInjectionPoints;
    }
}
