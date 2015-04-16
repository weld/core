/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.Container;
import org.jboss.weld.bean.AbstractProducerBean;
import org.jboss.weld.bean.builtin.AbstractBuiltInBean;
import org.jboss.weld.bean.builtin.ExtensionBean;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.exceptions.IllegalStateException;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.util.collections.SetMultimap;

/**
 * Probe is a per deployment service.
 *
 * <p>
 * An integrator is required to register this service and call {@link #initialize(BeanManagerImpl)} if appropriate.
 * </p>
 *
 * @author Martin Kouba
 */
public class Probe implements Service {

    // If needed make this configurable
    private static final int DEFAULT_INVOCATIONS_LIMIT = 5000;

    private volatile Mappings mappings;

    private final ConcurrentMap<Integer, Invocation> invocations;

    private final Comparator<Bean<?>> beanComparator;

    private final Comparator<ObserverMethod<?>> observerComparator;

    private final Comparator<BeanDeploymentArchive> bdaComparator;

    /**
     * Create a partially initialized instance.
     */
    public Probe() {
        this.invocations = new ConcurrentHashMap<Integer, Invocation>();
        this.beanComparator = new Comparator<Bean<?>>() {
            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                if (o1.getBeanClass().equals(o2.getBeanClass())) {
                    return mappings.getBeanToId().get(o1).compareTo(mappings.getBeanToId().get(o2));
                }
                return o1.getBeanClass().getName().compareTo(o2.getBeanClass().getName());
            }
        };
        this.observerComparator = new Comparator<ObserverMethod<?>>() {
            @Override
            public int compare(ObserverMethod<?> o1, ObserverMethod<?> o2) {
                if (o1.getBeanClass().equals(o2.getBeanClass())) {
                    return mappings.getObserverToId().get(o1).compareTo(mappings.getObserverToId().get(o2));
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
                        result = Components.getNumberOfEnabledBeans(mappings.getBdaToManager().get(bda2)).compareTo(
                                Components.getNumberOfEnabledBeans(mappings.getBdaToManager().get(bda1)));
                    }
                }
                // Unless decided compare the ids lexicographically
                return result == 0 ? bda1.getId().compareTo(bda2.getId()) : result;
            }
        };
    }

    /**
     * Initialize the service.
     *
     * @param beanManager
     */
    public void initialize(BeanManagerImpl beanManager) {
        if (isInitialized()) {
            throw new IllegalStateException("Probe already initialized!");
        }
        mappings = new Mappings(beanManager);
    }

    /**
     *
     * @return <code>true</code> if initialized, <code>false</code> otherwise
     */
    public boolean isInitialized() {
        return mappings != null;
    }

    /**
     *
     * @return an ordered list of all beans, including interceptors and decorators
     */
    List<Bean<?>> getBeans() {
        checkInitialized();
        List<Bean<?>> data = new ArrayList<Bean<?>>(mappings.getIdToBean().values());
        Collections.sort(data, beanComparator);
        return data;
    }

    /**
     *
     * @param bean
     * @return the generated id for the given bean
     */
    String getBeanId(Bean<?> bean) {
        checkInitialized();
        return mappings.getBeanToId().get(bean);
    }

    /**
     *
     * @param id
     * @return the bean for the given generated id
     */
    Bean<?> getBean(String id) {
        checkInitialized();
        return mappings.getIdToBean().get(id);
    }

    /**
     *
     * @param bean
     * @return the BeanManagerImpl for the given bean
     */
    BeanManagerImpl getBeanManager(Bean<?> bean) {
        checkInitialized();
        return mappings.getBeanToManager().get(bean);
    }

    /**
     *
     * @return an ordered list of all observers
     */
    List<ObserverMethod<?>> getObservers() {
        checkInitialized();
        List<ObserverMethod<?>> data = new ArrayList<ObserverMethod<?>>(mappings.getIdToObserver().values());
        Collections.sort(data, observerComparator);
        return data;
    }

    /**
     *
     * @param bean
     * @return the generated id for the given observer method
     */
    String getObserverId(ObserverMethod<?> observerMethod) {
        checkInitialized();
        return mappings.getObserverToId().get(observerMethod);
    }

    /**
     *
     * @param id
     * @return the observer method for the given generated id
     */
    ObserverMethod<?> getObserver(String id) {
        checkInitialized();
        return mappings.getIdToObserver().get(id);
    }

    /**
     *
     * @param invocation
     */
    void addInvocation(Invocation invocation) {
        checkInitialized();
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
        checkInitialized();
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
        checkInitialized();
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
        checkInitialized();
        int size = invocations.size();
        invocations.clear();
        return size;
    }

    /**
     *
     * @return the comparator for BDAs
     */
    Comparator<BeanDeploymentArchive> getBdaComparator() {
        return bdaComparator;
    }

    /**
     *
     * @param bean
     * @return the set of declared producers
     */
    Set<AbstractProducerBean<?, ?, ?>> getDeclaredProducers(Bean<?> bean) {
        return mappings.getBeanToDeclaredProducers().containsKey(bean) ? mappings.getBeanToDeclaredProducers().get(bean) : Collections.emptySet();
    }

    @Override
    public void cleanup() {
        if (mappings != null) {
            mappings.clear();
        }
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            throw ProbeLogger.LOG.probeNotInitialized();
        }
    }

    /**
     *
     * TODO make all data immutable
     *
     * @author Martin Kouba
     */
    private static class Mappings {

        private final Map<Bean<?>, String> beanToId;

        private final Map<String, Bean<?>> idToBean;

        private final Map<Bean<?>, BeanManagerImpl> beanToManager;

        private final Map<String, ObserverMethod<?>> idToObserver;

        private final Map<ObserverMethod<?>, String> observerToId;

        private final Map<BeanDeploymentArchive, BeanManagerImpl> bdaToManager;

        private final SetMultimap<Bean<?>, AbstractProducerBean<?, ?, ?>> beanToDeclaredProducers;

        Mappings(BeanManagerImpl beanManager) {

            beanToId = new HashMap<Bean<?>, String>();
            idToBean = new HashMap<String, Bean<?>>();
            beanToManager = new HashMap<Bean<?>, BeanManagerImpl>();
            idToObserver = new HashMap<String, ObserverMethod<?>>();
            observerToId = new HashMap<ObserverMethod<?>, String>();
            beanToDeclaredProducers = SetMultimap.newSetMultimap();

            ContextualStore contextualStore = beanManager.getServices().get(ContextualStore.class);
            bdaToManager = Container.instance(beanManager).beanDeploymentArchives();

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
        }

        Map<Bean<?>, String> getBeanToId() {
            return beanToId;
        }

        Map<String, Bean<?>> getIdToBean() {
            return idToBean;
        }

        Map<Bean<?>, BeanManagerImpl> getBeanToManager() {
            return beanToManager;
        }

        Map<String, ObserverMethod<?>> getIdToObserver() {
            return idToObserver;
        }

        Map<ObserverMethod<?>, String> getObserverToId() {
            return observerToId;
        }

        Map<BeanDeploymentArchive, BeanManagerImpl> getBdaToManager() {
            return bdaToManager;
        }

        SetMultimap<Bean<?>, AbstractProducerBean<?, ?, ?>> getBeanToDeclaredProducers() {
            return beanToDeclaredProducers;
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

        void clear() {
            idToBean.clear();
            beanToId.clear();
            beanToManager.clear();
            idToObserver.clear();
            observerToId.clear();
        }

    }

}
