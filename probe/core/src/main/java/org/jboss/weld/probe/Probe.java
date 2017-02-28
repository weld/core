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

import static org.jboss.weld.probe.Strings.ADDITIONAL_BDA_SUFFIX;
import static org.jboss.weld.probe.Strings.WEB_INF_CLASSES;
import static org.jboss.weld.util.reflection.Reflections.cast;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.injection.attributes.WeldInjectionPointAttributes;
import org.jboss.weld.injection.producer.AbstractMemberProducer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.Queries.BeanFilters;
import org.jboss.weld.probe.Queries.Filters;
import org.jboss.weld.probe.Queries.ObserverFilters;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.collections.SetMultimap;
import org.jboss.weld.util.reflection.Reflections;

/**
 * This component holds all the mapping and monitoring data.
 *
 * @author Martin Kouba
 */
@Vetoed
class Probe {

    // If needed make this configurable
    private static final int DEFAULT_INVOCATIONS_LIMIT = 5000;

    // If needed make this configurable
    private static final int DEFAULT_EVENTS_LIMIT = 5000;

    // Immutable mappings

    private final Map<Bean<?>, String> beanToId;

    private final Map<String, Bean<?>> idToBean;

    private final Map<Bean<?>, BeanManagerImpl> beanToManager;

    private final Map<String, ObserverMethod<?>> idToObserver;

    private final Map<ObserverMethod<?>, String> observerToId;

    private final Map<BeanDeploymentArchive, BeanManagerImpl> bdaToManager;

    private final SetMultimap<Bean<?>, AbstractProducerBean<?, ?, ?>> beanToDeclaredProducers;

    private final Set<Bean<?>> unusedBeans;

    // Monitoring data

    private final ConcurrentMap<Integer, Invocation> invocations;

    private final List<EventInfo> events;

    // Comparators

    private final Comparator<Bean<?>> beanComparator;

    private final Comparator<ObserverMethod<?>> observerComparator;

    private final Comparator<BeanDeploymentArchive> bdaComparator;

    private final AtomicLong initTs;

    private final BootstrapStats bootstrapStats;

    /**
     *
     */
    Probe() {
        this.initTs = new AtomicLong(0);
        this.invocations = new ConcurrentHashMap<Integer, Invocation>();
        this.events = Collections.synchronizedList(new LinkedList<EventInfo>());
        this.beanToId = new HashMap<Bean<?>, String>();
        this.idToBean = new HashMap<String, Bean<?>>();
        this.beanToManager = new HashMap<Bean<?>, BeanManagerImpl>();
        this.idToObserver = new HashMap<String, ObserverMethod<?>>();
        this.observerToId = new HashMap<ObserverMethod<?>, String>();
        this.beanToDeclaredProducers = SetMultimap.newSetMultimap();
        this.unusedBeans = new HashSet<>();
        this.bdaToManager = new HashMap<BeanDeploymentArchive, BeanManagerImpl>();
        this.beanComparator = new Comparator<Bean<?>>() {
            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                if (o1.getBeanClass().equals(o2.getBeanClass())) {
                    return beanToId.get(o1).compareTo(beanToId.get(o2));
                }
                return o1.getBeanClass().getName().compareTo(o2.getBeanClass().getName());
            }
        };
        this.observerComparator = new Comparator<ObserverMethod<?>>() {
            @Override
            public int compare(ObserverMethod<?> o1, ObserverMethod<?> o2) {
                if (o1.getBeanClass().equals(o2.getBeanClass())) {
                    return observerToId.get(o1).compareTo(observerToId.get(o2));
                }
                return o1.getBeanClass().getName().compareTo(o2.getBeanClass().getName());
            }
        };
        this.bdaComparator = new Comparator<BeanDeploymentArchive>() {
            @Override
            public int compare(BeanDeploymentArchive bda1, BeanDeploymentArchive bda2) {
                // Ids containing "WEB-INF/classes" have the highest priority
                int result = Boolean.compare(bda2.getId().contains(WEB_INF_CLASSES), bda1.getId().contains(WEB_INF_CLASSES));
                if (result == 0) {
                    // Additional bean archive should have the lowest priority when sorting
                    // This suffix is supported by WildFly and Weld Servlet
                    result = Boolean.compare(bda1.getId().endsWith(ADDITIONAL_BDA_SUFFIX), bda2.getId().endsWith(ADDITIONAL_BDA_SUFFIX));
                    if (result == 0) {
                        // Then order by number of enabled beans
                        result = Components.getNumberOfEnabledBeans(bdaToManager.get(bda2)).compareTo(
                                Components.getNumberOfEnabledBeans(bdaToManager.get(bda1)));
                    }
                }
                // Unless decided compare the ids lexicographically
                return result == 0 ? bda1.getId().compareTo(bda2.getId()) : result;
            }
        };
        this.bootstrapStats = new BootstrapStats();
    }

    /**
     *
     * @param beanManager
     */
    void init(BeanManagerImpl beanManager) {

        ContextualStore contextualStore = beanManager.getServices().get(ContextualStore.class);
        bdaToManager.putAll(Container.instance(beanManager).beanDeploymentArchives());

        for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : bdaToManager.entrySet()) {

            ProbeLogger.LOG.processingBeanDeploymentArchive(entry.getKey().getId());
            BeanManagerImpl manager = entry.getValue();

            // Beans
            for (Bean<?> bean : manager.getBeans()) {
                // Treat built-in beans (except for extensions) as one entity so that the dependency graph is more meaningful
                // E.g. Weld registers a separate InstanceBean for every bean deployment archive, from the user point of view
                // there's only one Instance bean though
                if (bean instanceof ExtensionBean) {
                    // ExtensionBean does not include BeanManager in its BeanIdentifier
                    ExtensionBean<?> extensionBean = (ExtensionBean<?>) bean;
                    if (!idToBean.containsValue(extensionBean)) {
                        putBean(Components.getId(extensionBean.getIdentifier()), manager, extensionBean);
                    }
                } else if (bean instanceof AbstractBuiltInBean<?>) {
                    // Built-in beans are identified by the set of types
                    String id = Components.getBuiltinBeanId((AbstractBuiltInBean<?>) bean);
                    if (!idToBean.containsKey(id)) {
                        putBean(id, bean);
                    }
                } else {
                    if (manager.isBeanEnabled(bean)) {
                        // Make sure the bean is truly enabled
                        putBean(contextualStore, manager, bean);
                    }
                }
            }
            // Interceptors
            for (Interceptor<?> interceptor : manager.getInterceptors()) {
                putBean(contextualStore, manager, interceptor);
            }
            // Decorators
            for (Decorator<?> decorator : manager.getDecorators()) {
                putBean(contextualStore, manager, decorator);
            }
            // Observers
            int customObservers = 0;
            for (ObserverMethod<?> observerMethod : manager.getObservers()) {
                if (observerMethod instanceof ObserverMethodImpl) {
                    ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
                    putObserver(Components.getId(observerMethodImpl.getId()), observerMethodImpl);
                } else {
                    // Custom observer methods
                    putObserver(Components.getId("" + customObservers++), observerMethod);
                }
            }
        }

        // Find declared producers
        for (Bean<?> candidate : idToBean.values()) {
            BeanKind kind = BeanKind.from(candidate);
            if ((BeanKind.PRODUCER_FIELD.equals(kind) || BeanKind.PRODUCER_METHOD.equals(kind) || BeanKind.RESOURCE.equals(kind))
                    && candidate instanceof AbstractProducerBean) {
                AbstractProducerBean<?, ?, ?> producerBean = (AbstractProducerBean<?, ?, ?>) candidate;
                beanToDeclaredProducers.put(producerBean.getDeclaringBean(), producerBean);
            }
        }

        findUnusedBeans();

        initTs.set(System.currentTimeMillis());
    }

    /**
     *
     * @return an ordered list of all beans, including interceptors and decorators
     */
    List<Bean<?>> getBeans() {
        List<Bean<?>> data = new ArrayList<Bean<?>>(idToBean.values());
        Collections.sort(data, beanComparator);
        return data;
    }

    /**
     *
     * @return an ordered list of beans
     */
    List<Bean<?>> getOrderedBeans(Set<Bean<?>> beans) {
        List<Bean<?>> data = new ArrayList<Bean<?>>(beans);
        Collections.sort(data, beanComparator);
        return data;
    }

    /**
     *
     * @param bean
     * @return the generated id for the given bean
     */
    String getBeanId(Bean<?> bean) {
        return beanToId.get(bean);
    }

    /**
     *
     * @param id
     * @return the bean for the given generated id
     */
    Bean<?> getBean(String id) {
        return idToBean.get(id);
    }

    /**
     *
     * @param bean
     * @return the BeanManagerImpl for the given bean
     */
    BeanManagerImpl getBeanManager(Bean<?> bean) {
        return beanToManager.get(bean);
    }

    /**
     *
     * @return an ordered list of all observers
     */
    List<ObserverMethod<?>> getObservers() {
        List<ObserverMethod<?>> observers = new ArrayList<ObserverMethod<?>>(idToObserver.values());
        Collections.sort(observers, observerComparator);
        return observers;
    }

    /**
     *
     * @param bean
     * @return the generated id for the given observer method
     */
    String getObserverId(ObserverMethod<?> observerMethod) {
        return observerToId.get(observerMethod);
    }

    /**
     *
     * @param id
     * @return the observer method for the given generated id
     */
    ObserverMethod<?> getObserver(String id) {
        return idToObserver.get(id);
    }

    /**
     *
     * @param bean
     * @return the set of declared producers
     */
    Set<AbstractProducerBean<?, ?, ?>> getDeclaredProducers(Bean<?> bean) {
        return beanToDeclaredProducers.containsKey(bean) ? beanToDeclaredProducers.get(bean) : Collections.emptySet();
    }

    /**
     *
     * @param invocation
     */
    void addInvocation(Invocation invocation) {
        if (!invocation.isEntryPoint()) {
            throw new IllegalStateException("Invocation is not an entry point!");
        }
        // Remove some old data if the limit is exceeded
        if (invocations.size() > DEFAULT_INVOCATIONS_LIMIT) {
            synchronized (this) {
                if (invocations.size() > DEFAULT_INVOCATIONS_LIMIT) {
                    Set<Integer> keySet = invocations.keySet();
                    List<Integer> sorted = new ArrayList<Integer>(keySet);
                    Collections.sort(sorted, Collections.reverseOrder());
                    if (keySet.removeAll(sorted.subList(DEFAULT_INVOCATIONS_LIMIT / 2, sorted.size()))) {
                        ProbeLogger.LOG.monitoringLimitExceeded(Invocation.class.getSimpleName(), DEFAULT_INVOCATIONS_LIMIT);
                    }
                }
            }
        }
        invocations.put(invocation.getEntryPointIdx(), invocation);
    }

    /**
     *
     * @return the sorted entry points (invocation trees)
     */
    List<Invocation> getInvocations() {
        List<Invocation> sorted = new ArrayList<Invocation>(invocations.values());
        Collections.sort(sorted, Invocation.Comparators.ENTRY_POINT_IDX);
        return sorted;
    }

    /**
     *
     * @param id
     * @return the invocation tree with the given generated id
     */
    Invocation getInvocation(String id) {
        try {
            return invocations.get(Integer.valueOf(id));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
   *
   */
    int clearInvocations() {
        int size = invocations.size();
        invocations.clear();
        return size;
    }

    void addEvent(EventInfo event) {
        // Remove some old data if the limit is exceeded
        if (events.size() > DEFAULT_EVENTS_LIMIT) {
            synchronized (this) {
                if (events.size() > DEFAULT_EVENTS_LIMIT) {
                    events.subList(0, DEFAULT_EVENTS_LIMIT / 2).clear();
                    ProbeLogger.LOG.monitoringLimitExceeded(EventInfo.class.getSimpleName(), DEFAULT_EVENTS_LIMIT);
                }
            }
        }
        events.add(event);
    }

    /**
     * Returns a mutable copy of the captured event information (in reverse order - last added events go first).
     *
     * @return mutable copy of the captured event information
     */
    List<EventInfo> getEvents() {
        synchronized (events) {
            List<EventInfo> result = new ArrayList<>(events.size());
            for (ListIterator<EventInfo> iterator = events.listIterator(events.size()); iterator.hasPrevious();) {
                result.add(iterator.previous());
            }
            return result;
        }
    }

    /**
     *
     * @return the number of captured events before the state is cleared.
     */
    int clearEvents() {
        synchronized (events) {
            int count = events.size();
            events.clear();
            return count;
        }
    }

    /**
     *
     * @return the comparator used for beans
     */
    Comparator<Bean<?>> getBeanComparator() {
        return beanComparator;
    }

    /**
     *
     * @return the comparator used for observer methods
     */
    Comparator<ObserverMethod<?>> getObserverComparator() {
        return observerComparator;
    }

    /**
     *
     * @return the comparator used for bean archives
     */
    Comparator<BeanDeploymentArchive> getBdaComparator() {
        return bdaComparator;
    }

    boolean isInitialized() {
        return initTs.get() == 0 ? false : true;
    }

    long getInitTs() {
        return initTs.get();
    }

    BootstrapStats getBootstrapStats() {
        return bootstrapStats;
    }

    /**
    *
    * @param id
    * @return the bean for the given generated id
    */
    BeanManagerImpl getBeanManager(String id) {
        for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : bdaToManager.entrySet()) {
            if (Components.getId(entry.getKey().getId()).equals(id)) {
                return entry.getValue();
            }
        }
        return null;
    }

    int getApplicationBeansCount() {
        return Queries.find(getBeans(), 0, 0, new BeanFilters(this, Filters.FILTER_ADDITIONAL_BDAS_MARKER)).getTotal();
    }

    int getApplicationObserversCount() {
        return Queries.find(getObservers(), 0, 0, new ObserverFilters(this, null, Filters.FILTER_ADDITIONAL_BDAS_MARKER)).getTotal();
    }

    int getInvocationsCount() {
        return invocations.size();
    }

    int getFiredEventsCount() {
        synchronized (events) {
            return events.size();
        }
    }

    boolean isUnused(Bean<?> bean) {
        return unusedBeans.contains(bean);
    }

    private void putBean(ContextualStore contextualStore, Bean<?> bean) {
        putBean(Components.getId(contextualStore.putIfAbsent(bean)), bean);
    }

    private void putBean(String id, Bean<?> bean) {
        idToBean.put(id, bean);
        beanToId.put(bean, id);
    }

    private void putBean(String id, BeanManagerImpl manager, Bean<?> bean) {
        putBean(id, bean);
        beanToManager.put(bean, manager);
    }

    private void putBean(ContextualStore contextualStore, BeanManagerImpl manager, Bean<?> bean) {
        putBean(contextualStore, bean);
        beanToManager.put(bean, manager);
    }

    private void putObserver(String id, ObserverMethod<?> observerMethod) {
        idToObserver.put(id, observerMethod);
        observerToId.put(observerMethod, id);
    }

    private void findUnusedBeans() {
        Collection<Bean<?>> beans = idToBean.values();
        Collection<ObserverMethod<?>> observers = idToObserver.values();
        for (Bean<?> bean : beans) {
            BeanKind kind = BeanKind.from(bean);
            if (BeanKind.BUILT_IN.equals(kind) || BeanKind.EXTENSION.equals(kind) || BeanKind.DECORATOR.equals(kind) || BeanKind.INTERCEPTOR.equals(kind)) {
                continue;
            }
            if (bean.getName() != null) {
                // Is annotated with @Named
                continue;
            }
            if (!(BeanKind.PRODUCER_FIELD.equals(kind) || BeanKind.PRODUCER_METHOD.equals(kind)) && !getDeclaredProducers(bean).isEmpty()) {
                // Has declared producers
                continue;
            }
            if (Components.hasDependents(bean, beans, this)) {
                // Has direct or potential (Instance<>) dependents
                continue;
            }
            if (hasDeclaredObserversOrIsInjectedIntoObserver(bean, observers)) {
                continue;
            }
            if (isInjectedIntoDisposer(bean, beans)) {
                continue;
            }
            unusedBeans.add(bean);
        }
    }

    private boolean isInjectedIntoDisposer(Bean<?> bean, Collection<Bean<?>> beans) {
        for (Bean<?> producerCandidate : beans) {
            if (producerCandidate instanceof AbstractProducerBean) {
                AbstractProducerBean<?, ?, ?> producerBean = cast(producerCandidate);
                if (producerBean.getProducer() instanceof AbstractMemberProducer<?, ?>) {
                    AbstractMemberProducer<?, ?> producer = Reflections.<AbstractMemberProducer<?, ?>> cast(producerBean.getProducer());
                    if (producer.getDisposalMethod() != null) {
                        BeanManager beanManager = getBeanManager(bean);
                        for (InjectionPoint injectionPoint : producer.getDisposalMethod().getInjectionPoints()) {
                            if (bean.equals(beanManager.resolve(beanManager.getBeans(injectionPoint.getType(),
                                    injectionPoint.getQualifiers().toArray(new Annotation[injectionPoint.getQualifiers().size()]))))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hasDeclaredObserversOrIsInjectedIntoObserver(Bean<?> bean, Collection<ObserverMethod<?>> observers) {
        for (ObserverMethod<?> observerMethod : observers) {
            if (observerMethod instanceof ObserverMethodImpl) {
                ObserverMethodImpl<?, ?> observerMethodImpl = (ObserverMethodImpl<?, ?>) observerMethod;
                if (bean.equals(observerMethodImpl.getDeclaringBean())) {
                    return true;
                }
                Set<WeldInjectionPointAttributes<?, ?>> injectionPoints = observerMethodImpl.getInjectionPoints();
                if (!injectionPoints.isEmpty()) {
                    BeanManager beanManager = getBeanManager(observerMethodImpl.getDeclaringBean());
                    if (beanManager != null) {
                        for (WeldInjectionPointAttributes<?, ?> injectionPoint : injectionPoints) {
                            if (bean.equals(beanManager.resolve(beanManager.getBeans(injectionPoint.getType(),
                                    injectionPoint.getQualifiers().toArray(new Annotation[injectionPoint.getQualifiers().size()]))))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    List<String> getLocalEnablementOfBean(Class<?> clazz) {
        List<String> localEnablementBDAIds = new ArrayList<>();
        for (Entry<BeanDeploymentArchive, BeanManagerImpl> entry : bdaToManager.entrySet()) {
            BeansXml beansXml = entry.getKey().getBeansXml();
            if (beansXml != null) {
                if (beansXml.getEnabledDecorators() != null && !beansXml.getEnabledDecorators().isEmpty()) {
                    for (Metadata<String> metadata : entry.getKey().getBeansXml().getEnabledDecorators()) {
                        if (metadata.getValue().equals(clazz.getName())) {
                            localEnablementBDAIds.add(entry.getKey().getId());
                        }
                    }
                }
                if (beansXml.getEnabledInterceptors() != null && !beansXml.getEnabledInterceptors().isEmpty()) {
                    for (Metadata<String> metadata : entry.getKey().getBeansXml().getEnabledInterceptors()) {
                        if (metadata.getValue().equals(clazz.getName())) {
                            localEnablementBDAIds.add(entry.getKey().getId());
                        }
                    }
                }
            }
        }
        return localEnablementBDAIds;
    }

}