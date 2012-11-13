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
package org.jboss.weld.bootstrap.events;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.attributes.ExternalBeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeployment;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.enablement.EnablementBuilder;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.bean.IsolatedForwardingBean;
import org.jboss.weld.util.bean.IsolatedForwardingDecorator;
import org.jboss.weld.util.bean.IsolatedForwardingInterceptor;
import org.jboss.weld.util.bean.WrappedBeanHolder;

public class AfterBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements AfterBeanDiscovery {

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts, EnablementBuilder enablementBuilder) {
        new AfterBeanDiscoveryImpl(beanManager, deployment, beanDeployments, contexts, enablementBuilder).fire();
    }

    protected AfterBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, Map<BeanDeploymentArchive, BeanDeployment> beanDeployments, Collection<ContextHolder<? extends Context>> contexts, EnablementBuilder enablementBuilder) {
        super(beanManager, AfterBeanDiscovery.class, beanDeployments, deployment, contexts, enablementBuilder);
    }

    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    public List<Throwable> getDefinitionErrors() {
        return Collections.unmodifiableList(getErrors());
    }

    public void addBean(Bean<?> bean) {
        processBean(bean);
    }

    protected <T> void processBean(Bean<T> b) {
        Bean<T> bean = b;
        BeanManagerImpl beanManager = getOrCreateBeanDeployment(bean.getBeanClass()).getBeanManager();
        ExternalBeanAttributesFactory.validateBeanAttributes(bean, beanManager);
        ContainerLifecycleEvents containerLifecycleEvents = beanManager.getServices().get(ContainerLifecycleEvents.class);

        // ProcessBeanAttributes for the Bean
        ProcessBeanAttributesImpl<T> event = containerLifecycleEvents.fireProcessBeanAttributes(beanManager, bean, null, bean.getBeanClass());
        if (event != null) {
            if (event.isVeto()) {
                return;
            }
            if (event.isDirty()) {
                bean = setBeanAttributes(bean, ExternalBeanAttributesFactory.of(event.getBeanAttributes(), getBeanManager()));
            }
        }

        if (bean instanceof Interceptor<?>) {
            beanManager.addInterceptor((Interceptor<?>) bean);
        } else if (bean instanceof Decorator<?>) {
            beanManager.addDecorator(CustomDecoratorWrapper.of((Decorator<?>) bean, beanManager));
        } else {
            beanManager.addBean(bean);
        }
        containerLifecycleEvents.fireProcessBean(beanManager, bean);
    }

    public void addContext(Context context) {
        getBeanManager().addContext(context);
    }

    public void addObserverMethod(ObserverMethod<?> observerMethod) {
        BeanManagerImpl manager = getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager();
        if (Observers.isObserverMethodEnabled(observerMethod, manager)) {
            ProcessObserverMethodImpl.fire(manager, observerMethod);
            manager.addObserver(observerMethod);
        }
        getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager().addObserver(observerMethod);
    }

    private <T> Bean<T> setBeanAttributes(final Bean<T> bean, final BeanAttributes<T> attributes) {
        if (bean instanceof Interceptor<?>) {
            return new IsolatedForwardingInterceptor.Impl<T>(WrappedBeanHolder.of(attributes, (Interceptor<T>) bean));
        }
        if (bean instanceof Decorator<?>) {
            return new IsolatedForwardingDecorator.Impl<T>(WrappedBeanHolder.of(attributes, (Decorator<T>) bean));
        }
        return new IsolatedForwardingBean.Impl<T>(WrappedBeanHolder.of(attributes, bean));
    }
}
