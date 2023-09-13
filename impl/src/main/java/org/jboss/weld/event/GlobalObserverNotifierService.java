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
package org.jboss.weld.event;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.BootstrapService;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.module.ObserverNotifierFactory;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.util.collections.Iterators;

/**
 * Hosts a {@link ObserverNotifier} that uses the global {@link TypeSafeObserverResolver} which has access to every enabled
 * observer method in the deployment. The underlying {@link ObserverNotifier} should be used every time an event is fired,
 * except for
 * special cases such as {@link jakarta.servlet.ServletContextEvent}, where the event is only fired to BDAs accessible from the
 * web archive.
 *
 * @author Jozef Hartinger
 *
 */
public class GlobalObserverNotifierService implements BootstrapService {

    private final Set<BeanManagerImpl> beanManagers;
    private final ObserverNotifier globalLenientObserverNotifier;
    private final ObserverNotifier globalStrictObserverNotifier;

    public GlobalObserverNotifierService(ServiceRegistry services, String contextId) {
        this.beanManagers = new CopyOnWriteArraySet<BeanManagerImpl>();
        TypeSafeObserverResolver resolver = new TypeSafeObserverResolver(services.get(MetaAnnotationStore.class),
                createGlobalObserverMethodIterable(beanManagers), services.get(WeldConfiguration.class));
        final ObserverNotifierFactory factory = services.get(ObserverNotifierFactory.class);
        this.globalLenientObserverNotifier = factory.create(contextId, resolver, services, false);
        this.globalStrictObserverNotifier = factory.create(contextId, resolver, services, true);
    }

    private static Iterable<ObserverMethod<?>> createGlobalObserverMethodIterable(final Set<BeanManagerImpl> beanManagers) {
        return new Iterable<ObserverMethod<?>>() {
            @Override
            public Iterator<ObserverMethod<?>> iterator() {
                Iterator<Iterator<ObserverMethod<?>>> observerMethodIterators = Iterators
                        .transform(beanManagers.iterator(), (beanManager) -> beanManager.getObservers().iterator());
                return Iterators.concat(observerMethodIterators);
            }
        };
    }

    public void registerBeanManager(BeanManagerImpl manager) {
        this.beanManagers.add(manager);
    }

    public ObserverNotifier getGlobalLenientObserverNotifier() {
        return globalLenientObserverNotifier;
    }

    public ObserverNotifier getGlobalStrictObserverNotifier() {
        return globalStrictObserverNotifier;
    }

    public Iterable<ObserverMethod<?>> getAllObserverMethods() {
        return createGlobalObserverMethodIterable(beanManagers);
    }

    @Override
    public void cleanupAfterBoot() {
        this.globalStrictObserverNotifier.clear();
        this.globalLenientObserverNotifier.clear();
    }

    @Override
    public void cleanup() {
        cleanupAfterBoot();
        this.beanManagers.clear();
    }
}
