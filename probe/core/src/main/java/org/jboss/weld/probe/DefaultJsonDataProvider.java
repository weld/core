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

import static org.jboss.weld.probe.Strings.BDA_ID;
import static org.jboss.weld.probe.Strings.ERROR;
import static org.jboss.weld.probe.Strings.QUALIFIERS;
import static org.jboss.weld.probe.Strings.REMOVED_EVENTS;
import static org.jboss.weld.probe.Strings.REMOVED_INVOCATIONS;
import static org.jboss.weld.probe.Strings.REQUIRED_TYPE;
import static org.jboss.weld.probe.Strings.RESOLVE;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Queries.BeanFilters;
import org.jboss.weld.probe.Queries.EventsFilters;
import org.jboss.weld.probe.Queries.InvocationsFilters;
import org.jboss.weld.probe.Queries.ObserverFilters;
import org.jboss.weld.probe.Resource.Representation;
import org.jboss.weld.resolution.QualifierInstance;
import org.jboss.weld.resolution.Resolvable;
import org.jboss.weld.resolution.ResolvableBuilder;
import org.jboss.weld.resources.spi.ResourceLoader;

/**
 *
 * @author Martin Kouba
 */
@Vetoed
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
        return (bean != null) ? JsonObjects.createFullBeanJson(bean, transientDependencies, transientDependents, beanManager, probe).build() : null;
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
    public String receiveObservers(int pageIndex, int pageSize, String filters, String representation) {
        return JsonObjects.createObserversJson(
                Queries.find(probe.getObservers(), pageIndex, pageSize, Queries.initFilters(filters, new ObserverFilters(probe))), probe,
                Representation.from(representation));
    }

    @Override
    public String receiveObserver(String id) {
        ObserverMethod<?> observer = probe.getObserver(id);
        if (observer != null) {
            return JsonObjects.createFullObserverJson(observer, probe).build();
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
    public String receiveInvocations(int pageIndex, int pageSize, String filters, String representation) {
        return JsonObjects.createInvocationsJson(
                Queries.find(probe.getInvocations(), pageIndex, pageSize, Queries.initFilters(filters, new InvocationsFilters(probe))), probe,
                Representation.from(representation));
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

    @Override
    public String receiveMonitoringStats() {
        return JsonObjects.createMonitoringStatsJson(probe).build();
    }

    @Override
    public String receiveAvailableBeans(int pageIndex, int pageSize, String filters, String representation) {
        Map<String, String> filterValues = Queries.Filters.parseFilters(filters);
        // First validate input
        if (!filterValues.containsKey(BDA_ID)) {
            return getError("Bean deployment archive id (bdaId) must be specified: " + filters);
        }
        // Find the bean deployment archive
        BeanManagerImpl beanManager = probe.getBeanManager(filterValues.get(BDA_ID));
        if (beanManager == null) {
            return getError("Unable to find the bean deployment archive for: " + filterValues.get(BDA_ID));
        }
        Set<Bean<?>> beans = null;
        // Parse required type
        Type requiredType;
        if (filterValues.get(REQUIRED_TYPE) != null) {
            requiredType = Parsers.parseType(filterValues.get(REQUIRED_TYPE), beanManager.getServices().get(ResourceLoader.class));
        } else {
            requiredType = Object.class;
        }
        if (requiredType == null) {
            return getError("Invalid required type: parsing error or the type is not accessible from the specified bean archive!");
        }
        ResolvableBuilder resolvableBuilder = new ResolvableBuilder(requiredType, beanManager);
        // Parse qualifiers
        List<QualifierInstance> qualifierInstances;
        if (filterValues.get(QUALIFIERS) != null) {
            qualifierInstances = Parsers.parseQualifiers(filterValues.get(QUALIFIERS), beanManager.getServices().get(ResourceLoader.class), beanManager);
        } else {
            qualifierInstances = Collections.singletonList(QualifierInstance.DEFAULT);
        }
        for (QualifierInstance qualifierInstance : qualifierInstances) {
            if (qualifierInstance == null) {
                return getError("Invalid qualifiers: parsing error or one of the qualifier types is not accessible from the specified bean archive!");
            }
            resolvableBuilder.addQualifierUnchecked(qualifierInstance);
        }
        Resolvable resolvable = resolvableBuilder.create();
        // Lookup beans
        beans = beanManager.getBeanResolver().resolve(resolvable, false);
        if (Boolean.valueOf(filterValues.get(RESOLVE))) {
            // Apply resolution rules
            beans = beanManager.getBeanResolver().resolve(beans);
        }
        return JsonObjects.createBeansJson(Queries.find(probe.getOrderedBeans(beans), pageIndex, pageSize, null), probe, beanManager,
                Representation.from(representation));
    }

    private String getError(String description) {
        return Json.objectBuilder().add(ERROR, description).build();
    }

}
