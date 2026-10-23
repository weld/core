/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.resolution;

import static org.junit.Assert.assertEquals;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.bootstrap.spi.Metadata;
import org.jboss.weld.config.WeldConfiguration;
import org.junit.Test;

public class TypeSafeObserverResolverTest {

    @Test
    public void testNegativeAndMaximumPriorities() {
        assertSortedInBothInputOrders(-1, Integer.MAX_VALUE);
    }

    @Test
    public void testMinimumAndMaximumPriorities() {
        assertSortedInBothInputOrders(Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Test
    public void testMinimumAndZeroPriorities() {
        assertSortedInBothInputOrders(Integer.MIN_VALUE, 0);
    }

    @Test
    public void testEqualPriorities() {
        ObserverMethod<?> first = new TestObserver(ObserverMethod.DEFAULT_PRIORITY);
        ObserverMethod<?> second = new TestObserver(ObserverMethod.DEFAULT_PRIORITY);
        assertSorted(List.of(first, second), List.of(first, second));
        assertSorted(List.of(second, first), List.of(second, first));
    }

    private void assertSortedInBothInputOrders(int lowerPriority, int higherPriority) {
        ObserverMethod<?> lower = new TestObserver(lowerPriority);
        ObserverMethod<?> higher = new TestObserver(higherPriority);
        assertSorted(List.of(lower, higher), List.of(lower, higher));
        assertSorted(List.of(higher, lower), List.of(lower, higher));
    }

    private void assertSorted(List<ObserverMethod<?>> input, List<ObserverMethod<?>> expected) {
        ServiceRegistry services = new SimpleServiceRegistry();
        Deployment deployment = new Deployment() {
            @Override
            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives() {
                return Collections.emptyList();
            }

            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                throw new UnsupportedOperationException();
            }

            @Override
            public ServiceRegistry getServices() {
                return services;
            }

            @Override
            public Iterable<Metadata<Extension>> getExtensions() {
                return Collections.emptyList();
            }
        };
        TypeSafeObserverResolver resolver = new TypeSafeObserverResolver(null, Collections.emptyList(),
                new WeldConfiguration(services, deployment));
        // Preserve the input order so overflow failures do not depend on HashSet iteration order.
        assertEquals(expected, resolver.sortResult(new LinkedHashSet<>(input)));
    }

    private static class TestObserver implements ObserverMethod<Object> {

        private final int priority;

        private TestObserver(int priority) {
            this.priority = priority;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Class<?> getBeanClass() {
            return TestObserver.class;
        }

        @Override
        public Type getObservedType() {
            return Object.class;
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Reception getReception() {
            return Reception.ALWAYS;
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return TransactionPhase.IN_PROGRESS;
        }

        @Override
        public void notify(Object event) {
            throw new UnsupportedOperationException();
        }
    }
}
