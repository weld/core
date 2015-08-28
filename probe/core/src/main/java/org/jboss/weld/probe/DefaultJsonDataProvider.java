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
package org.jboss.weld.probe;

import static org.jboss.weld.probe.Strings.REMOVED_EVENTS;
import static org.jboss.weld.probe.Strings.REMOVED_INVOCATIONS;

import java.lang.annotation.Annotation;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Queries.BeanFilters;
import org.jboss.weld.probe.Queries.EventsFilters;
import org.jboss.weld.probe.Queries.InvocationsFilters;
import org.jboss.weld.probe.Queries.ObserverFilters;
import org.jboss.weld.probe.Resource.Representation;

/**
 *
 * @author Martin Kouba
 */
public class DefaultJsonDataProvider implements JsonDataProvider {

    private final Probe probe;

    private final BeanManagerImpl beanManager;

    DefaultJsonDataProvider(Probe probe, BeanManagerImpl beanManager) {
        this.probe = probe;
        this.beanManager = beanManager;
    }

    @Override
    public String receiveDeployment() {
        return JsonObjects.createDeploymentJson(beanManager, probe);
    }

    @Override
    public String receiveBeans(int pageIndex, int pageSize, String filters, String representation) {
        return JsonObjects.createBeansJson(Queries.find(probe.getBeans(), pageIndex, pageSize, Queries.initFilters(filters, new BeanFilters(probe))), probe,
                beanManager, Representation.from(representation));
    }

    @Override
    public String receiveBean(String id, boolean transientDependencies, boolean transientDependents) {
        Bean<?> bean = probe.getBean(id);
        return (bean != null) ? JsonObjects.createFullBeanJson(bean, transientDependencies, transientDependents, beanManager, probe) : null;
    }

    @Override
    public String receiveBeanInstance(String id) {
        Bean<?> bean = probe.getBean(id);
        if (bean != null && Components.isInspectableScope(bean.getScope())) {
            Object instance = Components.findContextualInstance(bean, beanManager);
            if (instance != null) {
                return JsonObjects.createContextualInstanceJson(bean, instance, probe);
            }
        }
        return null;
    }

    @Override
    public String receiveObservers(int pageIndex, int pageSize, String filters) {
        return JsonObjects
                .createObserversJson(Queries.find(probe.getObservers(), pageIndex, pageSize, Queries.initFilters(filters, new ObserverFilters(probe))), probe);
    }

    @Override
    public String receiveObserver(String id) {
        ObserverMethod<?> observer = probe.getObserver(id);
        if (observer != null) {
            return JsonObjects.createFullObserverJson(observer, probe);
        }
        return null;
    }

    @Override
    public String receiveContexts() {
        return JsonObjects.createContextsJson(beanManager, probe).build();
    }

    @Override
    public String receiveContext(String id) {
        final Class<? extends Annotation> scope = Components.INSPECTABLE_SCOPES.get(id);
        if (scope != null) {
            return JsonObjects.createContextJson(id, scope, beanManager, probe, null).build();
        }
        return null;
    }

    @Override
    public String receiveInvocations(int pageIndex, int pageSize, String filters) {
        return JsonObjects.createInvocationsJson(
                Queries.find(probe.getInvocations(), pageIndex, pageSize, Queries.initFilters(filters, new InvocationsFilters(probe))), probe);
    }

    @Override
    public String clearInvocations() {
        return Json.objectBuilder().add(REMOVED_INVOCATIONS, probe.clearInvocations()).build();
    }

    @Override
    public String receiveInvocation(String id) {
        Invocation entryPoint = probe.getInvocation(id);
        if (entryPoint != null) {
            return JsonObjects.createFullInvocationJson(entryPoint, probe).build();
        }
        return null;
    }

    @Override
    public String receiveEvents(int pageIndex, int pageSize, String filters) {
        return JsonObjects.createEventsJson(Queries.find(probe.getEvents(), pageIndex, pageSize, Queries.initFilters(filters, new EventsFilters(probe))),
                probe);
    }

    @Override
    public String clearEvents() {
        return Json.objectBuilder().add(REMOVED_EVENTS, probe.clearEvents()).build();
    }

}
