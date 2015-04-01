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
import static org.jboss.weld.probe.Strings.APPLICATION;
import static org.jboss.weld.probe.Strings.BDA;
import static org.jboss.weld.probe.Strings.BEAN_CLASS;
import static org.jboss.weld.probe.Strings.BEAN_TYPE;
import static org.jboss.weld.probe.Strings.CONTAINER;
import static org.jboss.weld.probe.Strings.DESCRIPTION;
import static org.jboss.weld.probe.Strings.IS_ALTERNATIVE;
import static org.jboss.weld.probe.Strings.KIND;
import static org.jboss.weld.probe.Strings.METHOD_NAME;
import static org.jboss.weld.probe.Strings.OBSERVED_TYPE;
import static org.jboss.weld.probe.Strings.QUALIFIER;
import static org.jboss.weld.probe.Strings.RECEPTION;
import static org.jboss.weld.probe.Strings.SCOPE;
import static org.jboss.weld.probe.Strings.SEARCH;
import static org.jboss.weld.probe.Strings.STEREOTYPES;
import static org.jboss.weld.probe.Strings.TX_PHASE;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;
import org.jboss.weld.probe.ProbeObserver.EventInfo;

/**
 * A few utility methods and classes to support simple querying (filtering and pagination).
 *
 * @author Martin Kouba
 */
final class Queries {

    // TODO change to higher value, low value for dev purpose only
    static final int DEFAULT_PAGE_SIZE = 50;

    private Queries() {
    }

    /**
     * @param data
     * @param page
     * @param filters
     * @return the page of data
     */
    static <T, F extends Filters<T>> Page<T> find(List<T> data, int page, int pageSize, F filters) {
        if (filters != null) {
            ProbeLogger.LOG.filtersApplied(filters);
            for (Iterator<T> iterator = data.iterator(); iterator.hasNext();) {
                T element = iterator.next();
                if (!filters.test(element)) {
                    iterator.remove();
                }
            }
        }
        if (pageSize == 0) {
            return new Page<T>(page, 1, data.size(), data);
        } else {
            if (data.isEmpty()) {
                return new Page<T>(0, 0, 0, Collections.emptyList());
            }
            if ((page <= 0) || (page > 1 && (((page - 1) * pageSize) >= data.size()))) {
                page = 1;
            }
            int lastIdx = data.size() / pageSize;
            if (data.size() % pageSize > 0) {
                lastIdx++;
            }
            if (lastIdx == 1) {
                return new Page<T>(1, lastIdx, data.size(), data);
            }
            int start = (page - 1) * pageSize;
            int end = start + pageSize;
            if (end > data.size()) {
                end = data.size();
            }
            return new Page<T>(page, lastIdx, data.size(), data.subList(start, end));
        }
    }

    /**
     * A data page abstraction.
     *
     * @param <T>
     * @author Martin Kouba
     */
    static class Page<T> {

        private final int idx;

        private final int lastIdx;

        private final int total;

        private final List<T> data;

        Page(int idx, int lastIdx, int total, List<T> data) {
            this.idx = idx;
            this.lastIdx = lastIdx;
            this.total = total;
            this.data = data;
        }

        int getIdx() {
            return idx;
        }

        int getLastIdx() {
            return lastIdx;
        }

        int getTotal() {
            return total;
        }

        List<T> getData() {
            return data;
        }

    }

    /**
     * @param <T>
     * @author Martin Kouba
     */
    abstract static class Filters<T> {

        private static final String SEPARATOR = ":";

        protected static final String FILTER_ADDITIONAL_BDAS_MARKER = "probe-filterAdditionalBdas";

        protected final Probe probe;

        public Filters(Probe probe) {
            this.probe = probe;
        }

        /**
         * Input is a blank-separated list of key-value pairs. Keys and values are separated by {@value #SEPARATOR}. E.g.
         * <code>beanClass:name scope:myScope</code>.
         *
         * @param filters
         */
        void processFilters(String filters) {
            String[] tokens = filters.trim().split(" ");
            for (String token : tokens) {
                if (token.length() == 0) {
                    continue;
                }
                String[] parts = token.split(SEPARATOR);
                if (parts.length != 2) {
                    // Just ignore invalid tokens
                    continue;
                }
                processFilter(parts[0], parts[1]);
            }
        }

        abstract boolean test(T element);

        abstract void processFilter(String name, String value);

        /**
         * @param filter
         * @param value
         * @return true if the filter is null or equals to the value
         */
        boolean testEquals(Object filter, Object value) {
            return filter == null || filter.equals(value);
        }

        /**
         * @param filter
         * @param value
         * @return true if the filter is null or the value's {@link Object#toString()} contains the filter
         */
        boolean testContainsIgnoreCase(String filter, Object value) {
            return filter == null || containsIgnoreCase(filter, value);
        }

        /**
         * @param filter
         * @param value
         * @return true if the filter is null or any of the value's {@link Object#toString()} contains the filter
         */
        boolean testAnyContains(String filter, Collection<?> values) {
            if (filter == null) {
                return true;
            }
            for (Object value : values) {
                if (testContainsIgnoreCase(filter, value)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * @param bda
         * @param bean
         * @return true if the bda is null or the id of the BDA for the given bean equals to the value
         */
        boolean testBda(String bda, Bean<?> bean) {
            if (bda == null) {
                return true;
            }
            if (bean == null) {
                return false;
            }
            BeanManagerImpl beanManagerImpl = probe.getBeanManager(bean);
            if (beanManagerImpl == null) {
                return false;
            }
            if (FILTER_ADDITIONAL_BDAS_MARKER.equals(bda)) {
                if (beanManagerImpl.getId().endsWith(ADDITIONAL_BDA_SUFFIX)) {
                    return false;
                }
            } else {
                if (!Components.getId(beanManagerImpl.getId()).equals(bda)) {
                    return false;
                }
            }
            return true;
        }

        /**
         *
         * @param filter
         * @param value
         * @return true if the value's {@link Object#toString()} contains the filter
         */
        boolean containsIgnoreCase(String filter, Object value) {
            return value.toString().toLowerCase().contains(filter.toLowerCase());
        }

    }

    static class BeanFilters extends Filters<Bean<?>> {

        private BeanKind kind;

        private String beanClass;

        private String beanType;

        private String qualifier;

        private String scope;

        private String bda;

        private Boolean isAlternative;

        private String stereotypes;

        BeanFilters(Probe probe) {
            super(probe);
        }

        @Override
        public boolean test(Bean<?> bean) {
            return testBda(bda, bean) && testEquals(kind, BeanKind.from(bean)) && testContainsIgnoreCase(beanClass, bean.getBeanClass())
                    && testContainsIgnoreCase(scope, bean.getScope()) && testAnyContains(beanType, bean.getTypes())
                    && testAnyContains(qualifier, bean.getQualifiers()) && testEquals(isAlternative, bean.isAlternative())
                    && testAnyContains(stereotypes, bean.getStereotypes());
        }

        @Override
        void processFilter(String name, String value) {
            if (KIND.equals(name)) {
                kind = BeanKind.from(value);
            } else if (BEAN_CLASS.equals(name)) {
                beanClass = value;
            } else if (BEAN_TYPE.equals(name)) {
                beanType = value;
            } else if (QUALIFIER.equals(name)) {
                qualifier = value;
            } else if (SCOPE.equals(name)) {
                scope = value;
            } else if (BDA.equals(name)) {
                bda = value;
            } else if (IS_ALTERNATIVE.equals(name)) {
                isAlternative = Boolean.valueOf(value);
            } else if (STEREOTYPES.equals(name)) {
                stereotypes = value;
            }
        }

        @Override
        public String toString() {
            return String.format("BeanFilters [kind=%s, beanClass=%s, beanType=%s, qualifier=%s, scope=%s, bda=%s, isAlternative=%s, stereotypes=%s]", kind,
                    beanClass, beanType, qualifier, scope, bda, isAlternative, stereotypes);
        }

    }

    static class ObserverFilters extends Filters<ObserverMethod<?>> {

        private String beanClass;

        private String observedType;

        private String qualifier;

        private Reception reception;

        private TransactionPhase txPhase;

        private BeanKind declaringBeanKind;

        private String bda;

        ObserverFilters(Probe probe) {
            super(probe);
        }

        @Override
        boolean test(ObserverMethod<?> observer) {
            final Bean<?> declaringBean;
            if (observer instanceof ObserverMethodImpl) {
                declaringBean = ((ObserverMethodImpl<?, ?>) observer).getDeclaringBean();
            } else {
                declaringBean = null;
            }
            return testBda(bda, declaringBean) && testEquals(declaringBeanKind, BeanKind.from(declaringBean)) && testEquals(reception, observer.getReception())
                    && testEquals(txPhase, observer.getTransactionPhase()) && testContainsIgnoreCase(beanClass, observer.getBeanClass())
                    && testContainsIgnoreCase(observedType, observer.getObservedType()) && testAnyContains(qualifier, observer.getObservedQualifiers());
        }

        @Override
        void processFilter(String name, String value) {
            if (KIND.equals(name)) {
                declaringBeanKind = BeanKind.from(value);
            } else if (BEAN_CLASS.equals(name)) {
                beanClass = value;
            } else if (OBSERVED_TYPE.equals(name)) {
                observedType = value;
            } else if (QUALIFIER.equals(name)) {
                qualifier = value;
            } else if (RECEPTION.equals(name)) {
                for (Reception recept : Reception.values()) {
                    if (recept.toString().equals(value)) {
                        reception = recept;
                    }
                }
            } else if (TX_PHASE.equals(name)) {
                for (TransactionPhase phase : TransactionPhase.values()) {
                    if (phase.toString().equals(value)) {
                        txPhase = phase;
                    }
                }
            } else if (BDA.equals(name)) {
                bda = value;
            }
        }

        @Override
        public String toString() {
            return String.format("ObserverFilters [beanClass=%s, observedType=%s, qualifier=%s, reception=%s, txPhase=%s, declaringBeanKind=%s, bda=%s]",
                    beanClass, observedType, qualifier, reception, txPhase, declaringBeanKind, bda);
        }

    }

    static class InvocationsFilters extends Filters<Invocation> {

        private String beanClass;

        private String methodName;

        private String search;

        private String description;

        InvocationsFilters(Probe probe) {
            super(probe);
        }

        @Override
        boolean test(Invocation invocation) {
            return testSearch(search, invocation) && testContainsIgnoreCase(beanClass, invocation.getBeanClass())
                    && testContainsIgnoreCase(methodName, invocation.getMethodName()) && testContainsIgnoreCase(description, invocation.getDescription());
        }

        @Override
        void processFilter(String name, String value) {
            if (BEAN_CLASS.equals(name)) {
                beanClass = value;
            } else if (METHOD_NAME.equals(name)) {
                methodName = value;
            } else if (SEARCH.equals(name)) {
                search = value;
            } else if (DESCRIPTION.equals(name)) {
                description = value;
            }
        }

        boolean testSearch(String search, Invocation invocation) {
            if (search == null) {
                return true;
            }
            if (containsIgnoreCase(search, invocation.getBeanClass()) || containsIgnoreCase(search, invocation.getMethodName())) {
                return true;
            }
            if (invocation.hasChildren()) {
                for (Invocation child : invocation.getChildren()) {
                    if (testSearch(search, child)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("InvocationsFilters [beanClass=%s, methodName=%s, search=%s]", beanClass, methodName, search);
        }

    }

    static class EventsFilters extends Filters<EventInfo> {

        private Boolean container;
        private String eventInfo;
        private String type;
        private String qualifiers;

        EventsFilters(Probe probe) {
            super(probe);
        }

        @Override
        boolean test(EventInfo event) {
            return testContainsIgnoreCase(eventInfo, event.eventString) && testContainsIgnoreCase(type, event.type)
                    && testContainsIgnoreCase(qualifiers, event.qualifiers) && (container == null || container == event.containerEvent);
        }

        @Override
        void processFilter(String name, String value) {
            if (Strings.EVENT_INFO.equals(name)) {
                this.eventInfo = value;
            } else if (Strings.TYPE.equals(name)) {
                this.type = value;
            } else if (Strings.QUALIFIERS.equals(name)) {
                this.qualifiers = value;
            } else if (Strings.KIND.equals(name)) {
                if (CONTAINER.equalsIgnoreCase(value)) {
                    container = true;
                } else if (APPLICATION.equalsIgnoreCase(value)) {
                    container = false;
                }
            }
        }

        @Override
        public String toString() {
            return String.format("EventsFilters [container=%s, eventInfo=%s, type=%s, qualifiers=%s]", container, eventInfo, type, qualifiers);
        }
    }

}
