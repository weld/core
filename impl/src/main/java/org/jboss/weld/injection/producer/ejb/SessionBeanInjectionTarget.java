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
package org.jboss.weld.injection.producer.ejb;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;

import org.jboss.weld.annotated.enhanced.EnhancedAnnotatedType;
import org.jboss.weld.bean.SessionBean;
import org.jboss.weld.bean.interceptor.InterceptorBindingsAdapter;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.ConstructorInjectionPoint;
import org.jboss.weld.injection.InjectionContextImpl;
import org.jboss.weld.injection.InjectionPointFactory;
import org.jboss.weld.injection.producer.AbstractInjectionTarget;
import org.jboss.weld.injection.producer.Instantiator;
import org.jboss.weld.interceptor.spi.metadata.ClassMetadata;
import org.jboss.weld.interceptor.spi.model.InterceptionModel;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Beans;

public class SessionBeanInjectionTarget<T> extends AbstractInjectionTarget<T> {

    private final SessionBean<T> bean;

    public SessionBeanInjectionTarget(EnhancedAnnotatedType<T> type, SessionBean<T> bean, BeanManagerImpl beanManager) {
        super(type, bean, beanManager);
        this.bean = bean;
    }

    @Override
    public void initializeAfterBeanDiscovery(EnhancedAnnotatedType<T> annotatedType) {
        super.initializeAfterBeanDiscovery(annotatedType);
        List<Decorator<?>> decorators = beanManager.resolveDecorators(getBean().getTypes(), getBean().getQualifiers());
        if (!decorators.isEmpty()) {
            setInstantiator(new ProxyDecoratorApplyingSessionBeanInstantiator<T>(getInstantiator(), bean, decorators));
        }
        registerInterceptors();
    }

    protected void registerInterceptors() {
        InterceptionModel<ClassMetadata<?>, ?> model = beanManager.getInterceptorModelRegistry().get(bean.getEjbDescriptor().getBeanClass());
        if (model != null) {
            getBeanManager().getServices().get(EjbServices.class).registerInterceptors(bean.getEjbDescriptor(), new InterceptorBindingsAdapter(model));
        }
    }

    @Override
    public SessionBean<T> getBean() {
        return bean;
    }

    @Override
    public void inject(final T instance, final CreationalContext<T> ctx) {
        new InjectionContextImpl<T>(getBeanManager(), this, getType(), instance) {

            public void proceed() {
                Beans.injectFieldsAndInitializers(instance, ctx, getBeanManager(), getInjectableFields(), getInitializerMethods());
            }

        }.run();
    }

    @Override
    protected Instantiator<T> initInstantiator(EnhancedAnnotatedType<T> type, Bean<T> bean, BeanManagerImpl beanManager, Set<InjectionPoint> injectionPoints) {
        if (bean instanceof SessionBean<?>) {
            ConstructorInjectionPoint<T> originalConstructor = InjectionPointFactory.instance().createConstructorInjectionPoint(bean, type, beanManager);
            injectionPoints.addAll(originalConstructor.getParameterInjectionPoints());
            return new SessionBeanInstantiator<T>(type, (SessionBean<T>) bean);
        } else {
            throw new IllegalArgumentException("Cannot create SessionBeanInjectionTarget for " + bean);
        }
    }

}
