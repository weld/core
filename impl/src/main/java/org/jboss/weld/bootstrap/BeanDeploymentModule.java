/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.util.collections.Iterables.flatMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.event.DefaultObserverNotifierFactory;
import org.jboss.weld.event.EventMetadataImpl;
import org.jboss.weld.event.ObserverNotifier;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.resolution.TypeSafeObserverResolver;
import org.jboss.weld.util.collections.Iterables;

/**
 * Represents an EE module that holds bean archives. This is a war, ejb jar, ear/lib or possibly a different module. This
 * construct allows a given event to be
 * delivered to observers in the given module only. This is used for handling of {@link Initialized} and {@link Destroyed}
 * events for the application context.
 * This is a special case since the payload of this particular event is module specific (ServletContext for web modules, Object
 * otherwise).
 * This does not apply to other {@link Initialized} and {@link Destroyed} events which are delivered using an accessible
 * notifier.
 *
 * @author Jozef Hartinger
 *
 */
public final class BeanDeploymentModule {

    private final String id;
    private final boolean web;
    private final ObserverNotifier notifier;
    private final Set<BeanManagerImpl> managers;

    BeanDeploymentModule(String moduleId, String contextId, boolean web, ServiceRegistry services) {
        this.id = moduleId;
        this.web = web;
        this.managers = new CopyOnWriteArraySet<>();
        // create module-local observer notifier
        Iterable<ObserverMethod<?>> observers = flatMap(managers, BeanManagerImpl::getObservers);
        final TypeSafeObserverResolver resolver = new TypeSafeObserverResolver(services.get(MetaAnnotationStore.class),
                observers,
                services.get(WeldConfiguration.class));
        this.notifier = DefaultObserverNotifierFactory.INSTANCE.create(contextId, resolver, services, false);
    }

    /**
     * Identifier of the module
     *
     * @return the identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Indicates whether this module represents a web module
     *
     * @return true iff this module is a web module
     */
    public boolean isWebModule() {
        return web;
    }

    /**
     * Fire an event and notify observers that belong to this module.
     *
     * @param eventType
     * @param event
     * @param qualifiers
     */
    public void fireEvent(Type eventType, Object event, Annotation... qualifiers) {
        final EventMetadata metadata = new EventMetadataImpl(eventType, null, qualifiers);
        notifier.fireEvent(eventType, event, metadata, qualifiers);
    }

    void addManager(BeanManagerImpl manager) {
        if (this.managers.add(manager)) {
            notifier.clear();
        }
    }

    @Override
    public String toString() {
        return "BeanDeploymentModule [id=" + id + ", web=" + web + ", managers="
                + Iterables.transform(managers, BeanManagerImpl::getId) + "]";
    }
}
