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

import static org.jboss.weld.util.reflection.Reflections.cast;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.PassivationCapable;

import org.jboss.weld.annotated.slim.SlimAnnotatedTypeStore;
import org.jboss.weld.bean.CustomDecoratorWrapper;
import org.jboss.weld.bean.attributes.ExternalBeanAttributesFactory;
import org.jboss.weld.bootstrap.BeanDeploymentArchiveMapping;
import org.jboss.weld.bootstrap.ContextHolder;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.logging.BeanLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Observers;
import org.jboss.weld.util.Preconditions;

public class AfterBeanDiscoveryImpl extends AbstractBeanDiscoveryEvent implements AfterBeanDiscovery {

    private static final String TYPE_ARGUMENT_NAME = "type";

    public static void fire(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts) {
        new AfterBeanDiscoveryImpl(beanManager, deployment, bdaMapping, contexts).fire();
    }

    protected AfterBeanDiscoveryImpl(BeanManagerImpl beanManager, Deployment deployment, BeanDeploymentArchiveMapping bdaMapping, Collection<ContextHolder<? extends Context>> contexts) {
        super(beanManager, AfterBeanDiscovery.class, bdaMapping, deployment, contexts);
        this.slimAnnotatedTypeStore = beanManager.getServices().get(SlimAnnotatedTypeStore.class);
    }

    private final SlimAnnotatedTypeStore slimAnnotatedTypeStore;

    @Override
    public void addDefinitionError(Throwable t) {
        getErrors().add(t);
    }

    public List<Throwable> getDefinitionErrors() {
        return Collections.unmodifiableList(getErrors());
    }

    @Override
    public void addBean(Bean<?> bean) {
        processBean(bean);
    }

    protected <T> void processBean(Bean<T> b) {
        Bean<T> bean = b;
        BeanManagerImpl beanManager = getOrCreateBeanDeployment(bean.getBeanClass()).getBeanManager();
        ExternalBeanAttributesFactory.validateBeanAttributes(bean, beanManager);
        ContainerLifecycleEvents containerLifecycleEvents = beanManager.getServices().get(ContainerLifecycleEvents.class);

        if (bean instanceof PassivationCapable) {
            PassivationCapable passivationCapable = (PassivationCapable) bean;
            if (passivationCapable.getId() == null) {
                throw BeanLogger.LOG.passivationCapableBeanHasNullId(bean);
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

    @Override
    public void addContext(Context context) {
        getBeanManager().addContext(context);
    }

    @Override
    public void addObserverMethod(ObserverMethod<?> observerMethod) {
        BeanManagerImpl manager = getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager();
        if (Observers.isObserverMethodEnabled(observerMethod, manager)) {
            ProcessObserverMethodImpl.fire(manager, observerMethod);
            manager.addObserver(observerMethod);
        }
        getOrCreateBeanDeployment(observerMethod.getBeanClass()).getBeanManager().addObserver(observerMethod);
    }

    @Override
    public <T> AnnotatedType<T> getAnnotatedType(Class<T> type, String id) {
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return slimAnnotatedTypeStore.get(type, id);
    }

    @Override
    public <T> Iterable<AnnotatedType<T>> getAnnotatedTypes(Class<T> type) {
        Preconditions.checkArgumentNotNull(type, TYPE_ARGUMENT_NAME);
        return cast(slimAnnotatedTypeStore.get(type));
    }
}
