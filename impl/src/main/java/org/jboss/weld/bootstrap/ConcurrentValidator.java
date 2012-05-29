/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.messages.ValidatorMessage.AMBIGUOUS_EL_NAME;
import static org.jboss.weld.logging.messages.ValidatorMessage.BEAN_NAME_IS_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.Producer;

import org.jboss.weld.bean.RIBean;
import org.jboss.weld.exceptions.DeploymentException;
import org.jboss.weld.executor.IterativeWorkerTaskFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.manager.api.ExecutorServices;
import org.jboss.weld.util.Beans;
import org.jboss.weld.util.collections.HashSetSupplier;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 * Processes validation of beans, decorators and interceptors in parallel.
 *
 * @author Jozef Hartinger
 *
 */
public class ConcurrentValidator extends Validator {

    private final ExecutorServices executor;

    public ConcurrentValidator(ExecutorServices executor) {
        this.executor = executor;
    }

    @Override
    public void validateBeans(Collection<? extends Bean<?>> beans, final BeanManagerImpl manager) {
        final List<RuntimeException> problems = new CopyOnWriteArrayList<RuntimeException>();
        final Set<RIBean<?>> specializedBeans = Sets.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>());

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Bean<?>>(beans) {
            protected void doWork(Bean<?> bean) {
                validateBean(bean, specializedBeans, manager, problems);
            }
        });

        if (!problems.isEmpty()) {
            if (problems.size() == 1) {
                throw problems.get(0);
            } else {
                throw new DeploymentException(problems);
            }
        }
    }

    @Override
    public void validateInterceptors(Collection<? extends Interceptor<?>> interceptors) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Interceptor<?>>(interceptors) {
            protected void doWork(Interceptor<?> interceptor) {
                validateInterceptor(interceptor);
            }
        });
    }

    @Override
    public void validateDecorators(Collection<? extends Decorator<?>> decorators, final BeanManagerImpl manager) {
        final Set<RIBean<?>> specializedBeans = Sets.newSetFromMap(new ConcurrentHashMap<RIBean<?>, Boolean>());

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Decorator<?>>(decorators) {
            protected void doWork(Decorator<?> decorator) {
                validateDecorator(decorator, specializedBeans, manager);
            }
        });
    }

    @Override
    protected void validateObserverMethods(Iterable<ObserverInitializationContext<?, ?>> observers, final BeanManagerImpl beanManager) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<ObserverInitializationContext<?, ?>>(observers) {
            protected void doWork(ObserverInitializationContext<?, ?> observerMethod) {
                for (InjectionPoint ip : observerMethod.getObserver().getInjectionPoints()) {
                    validateInjectionPoint(ip, beanManager);
                }
            }
        });
    }

    @Override
    public void validateBeanNames(final BeanManagerImpl beanManager) {
        final SetMultimap<String, Bean<?>> namedAccessibleBeans = Multimaps.newSetMultimap(new HashMap<String, Collection<Bean<?>>>(), HashSetSupplier.<Bean<?>> instance());

        for (Bean<?> bean : beanManager.getAccessibleBeans()) {
            if (bean.getName() != null) {
                namedAccessibleBeans.put(bean.getName(), bean);
            }
        }

        final List<String> accessibleNamespaces = new ArrayList<String>();
        for (String namespace : beanManager.getAccessibleNamespaces()) {
            accessibleNamespaces.add(namespace);
        }

        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<String>(namedAccessibleBeans.keySet()) {
            protected void doWork(String name) {
                Set<Bean<?>> resolvedBeans = beanManager.getBeanResolver().resolve(Beans.removeDisabledAndSpecializedBeans(namedAccessibleBeans.get(name), beanManager));
                if (resolvedBeans.size() > 1) {
                    throw new DeploymentException(AMBIGUOUS_EL_NAME, name, resolvedBeans);
                }
                if (accessibleNamespaces.contains(name)) {
                    throw new DeploymentException(BEAN_NAME_IS_PREFIX, name);
                }
            }
        });
    }

    @Override
    public void validateProducers(Collection<Producer<?>> producers, final BeanManagerImpl beanManager) {
        executor.invokeAllAndCheckForExceptions(new IterativeWorkerTaskFactory<Producer<?>>(producers) {
            protected void doWork(Producer<?> producer) {
                validateProducer(producer, beanManager);
            }
        });
    }
}
