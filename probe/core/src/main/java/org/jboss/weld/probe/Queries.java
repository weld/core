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
import static org.jboss.weld.probe.Strings.UNUSED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.Vetoed;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.event.ObserverMethodImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.probe.Components.BeanKind;

/**
 * A few utility methods and classes to support simple querying (filtering and pagination).
 *
 * @author Martin Kouba
 */
@Vetoed
final class Queries {

    static final int DEFAULT_PAGE_SIZE = 50;

    private Queries() {
    }

    /**
     * @param data
     * @param page
     * @param pageSize
     * @param filters
     * @return the page of data
     */
    static <T, F extends Filters<T>> Page<T> find(List<T> data, int page, int pageSize, F filters) {
        if (filters != null && !filters.isEmpty()) {
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

    static <E, T extends Filters<E>> T initFilters(String filtersParam, T uninitializedFilters) {
        if (filtersParam == null || filtersParam.trim().length() == 0) {
            return null;
        }
        uninitializedFilters.initialize(filtersParam);
        return uninitializedFilters;
    }

    /**
     * @param <T>
     * @author Martin Kouba
     */
    abstract static class Filters<T> {

        private static final char PAIR_SEPARATOR = ' ';

        private static final char KEY_VALUE_SEPARATOR = ':';

        private static final char VALUE_QUOTE = '"';

        static final String FILTER_ADDITIONAL_BDAS_MARKER = "probe-filterAdditionalBdas";

        protected final Probe probe;

        static Map<String, String> parseFilters(String filters) {

            List<String> pairs = new ArrayList<String>();
            boolean inSeparator = false;
            boolean inValueLiteral = false;
            StringBuilder buffer = new StringBuilder();

            for (int i = 0; i < filters.length(); i++) {
                if (filters.charAt(i) == PAIR_SEPARATOR) {
                    if (!inSeparator) {
                        if (!inValueLiteral) {
                            if (buffer.length() > 0) {
                                pairs.add(buffer.toString());
                                buffer = new StringBuilder();
                            }
                            inSeparator = true;
                        } else {
                            buffer.append(filters.charAt(i));
                        }
                    }
                } else {
                    if (filters.charAt(i) == VALUE_QUOTE && (i == 0 || filters.charAt(i - 1) == KEY_VALUE_SEPARATOR || i + 1 >= filters.length()
                            || filters.charAt(i + 1) == PAIR_SEPARATOR)) {
                        inValueLiteral = !inValueLiteral;
                    }
                    inSeparator = false;
                    buffer.append(filters.charAt(i));
                }
            }

            if (buffer.length() > 0) {
                if (inValueLiteral) {
                    throw ProbeLogger.LOG.unableToParseQueryFilter(filters);
                }
                pairs.add(buffer.toString());
            }

            Map<String, String> map = new HashMap<>();
            for (String pair : pairs) {
                if (pair.length() == 0) {
                    continue;
                }
                int separator = pair.indexOf(KEY_VALUE_SEPARATOR);
                if (separator == -1) {
                    continue;
                }
                String key = pair.substring(0, separator);
                String value = pair.substring(separator + 1, pair.length());
                map.put(key, value.substring(1, value.length() - 1));
            }
            return map;
        }

        public Filters(Probe probe) {
            this.probe = probe;
        }

        /**
         * Input is a blank-separated list of key-value pairs. Keys and values are separated by {@value #KEY_VALUE_SEPARATOR}. Values must be enclosed in
         * quotation marks. E.g. <code>beanClass:"com.foo.Name" scope:"@MyScoped"</code>.
         *
         * @param filters
         */
        void initialize(String filters) {
            for (Entry<String, String> entry : parseFilters(filters).entrySet()) {
                processFilter(entry.getKey(), entry.getValue());
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
            return value.toString().toLowerCase().contains(prepareFilter(filter).toLowerCase());
        }

        private String prepareFilter(String filter) {
            // Expect simplified annotation representation, e.g. @Dependent
            return filter.startsWith("@") ? filter.substring(1, filter.length()) : filter;
        }

        abstract boolean isEmpty();

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

        private boolean unused;

        BeanFilters(Probe probe) {
            super(probe);
        }

        BeanFilters(Probe probe, String bda) {
            super(probe);
            this.bda = bda;
        }

        @Override
        public boolean test(Bean<?> bean) {
            return testBda(bda, bean) && testEquals(kind, BeanKind.from(bean)) && testContainsIgnoreCase(beanClass, bean.getBeanClass())
                    && testContainsIgnoreCase(scope, bean.getScope()) && testAnyContains(beanType, bean.getTypes())
                    && testAnyContains(qualifier, bean.getQualifiers()) && testEquals(isAlternative, bean.isAlternative())
                    && testAnyContains(stereotypes, bean.getStereotypes()) && testUnused(bean);
        }

        boolean testUnused(Bean<?> bean) {
            return !unused || probe.isUnused(bean);
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
            } else if (UNUSED.equals(name) && Boolean.valueOf(value)) {
                unused = true;
            }
        }

        @Override
        public String toString() {
            return String.format("BeanFilters [kind=%s, beanClass=%s, beanType=%s, qualifier=%s, scope=%s, bda=%s, isAlternative=%s, stereotypes=%s, unused=%s]", kind,
                    beanClass, beanType, qualifier, scope, bda, isAlternative, stereotypes, unused);
        }

        @Override
        boolean isEmpty() {
            return kind == null && beanClass == null && beanType == null && qualifier == null && scope == null && bda == null && isAlternative == null
                    && stereotypes == null && !unused;
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

        ObserverFilters(Probe probe, String observedType, String bda) {
            super(probe);
            this.observedType = observedType;
            this.bda = bda;
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

        @Override
        boolean isEmpty() {
            return beanClass == null && observedType == null && qualifier == null && reception == null && bda == null && txPhase == null
                    && declaringBeanKind == null;
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
            return String.format("InvocationsFilters [beanClass=%s, methodName=%s, search=%s, description=%s]", beanClass, methodName, search, description);
        }

        @Override
        boolean isEmpty() {
            return beanClass == null && methodName == null && search == null && description == null;
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
            return testContainsIgnoreCase(eventInfo, event.getEventString()) && testContainsIgnoreCase(type, event.getType())
                    && testAnyContains(qualifiers, event.getQualifiers()) && (container == null || container == event.isContainerEvent());
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

        @Override
        boolean isEmpty() {
            return container == null && eventInfo == null && type == null && qualifiers == null;
        }

    }

}
