/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.environment.se.test.synthetic.priority;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Reserve;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.inject.Inject;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SyntheticBeanPriorityIsolationTest {
    @Parameters(name = "reserve={0}, configurator={1}, reverse={2}")
    public static Object[][] parameters() {
        return new Object[][] {
                { false, false, false }, { false, false, true },
                { true, false, false }, { true, false, true },
                { false, true, false }, { false, true, true },
                { true, true, false }, { true, true, true }
        };
    }

    private final boolean reserve;
    private final boolean configurator;
    private final boolean reverse;

    public SyntheticBeanPriorityIsolationTest(boolean reserve, boolean configurator, boolean reverse) {
        this.reserve = reserve;
        this.configurator = configurator;
        this.reverse = reverse;
    }

    @Test
    public void distinctClassesSelectHigherPriority() {
        check(false, false, false);
    }

    @Test
    public void unrelatedBeanDoesNotChangeWinner() {
        check(false, true, false);
    }

    @Test
    public void sharedClassSelectsHigherPriority() {
        check(true, false, false);
    }

    @Test
    public void equalPrioritiesRemainAmbiguous() {
        check(true, false, true);
    }

    @Test
    public void priorityDoesNotEnableAnotherSyntheticBeanWithSharedClass() {
        Extension extension = new Extension() {
            void register(@Observes AfterBeanDiscovery event) {
                TestBean<OtherService> prioritized = new TestBean<>(SharedMarker.class, OtherService.class,
                        () -> () -> "C", 20, reserve);
                registerBean(event, prioritized);
                event.addBean().beanClass(SharedMarker.class).types(Service.class)
                        .alternative(!reserve).reserve(reserve).createWith(ctx -> (Service) () -> "disabled");
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClass(Consumer.class)
                .addExtension(extension).initialize()) {
            assertTrue(container.select(Consumer.class).get().services.isUnsatisfied());
        }
    }

    @Test
    public void ordinaryBeanSuppressesReserves() {
        assumeTrue(reserve);
        Extension extension = new Extension() {
            void register(@Observes AfterBeanDiscovery event) {
                registerBean(event, new TestBean<Service>(SharedMarker.class, Service.class, () -> () -> "reserve",
                        20, true));
                event.addBean().beanClass(SharedMarker.class).types(Service.class)
                        .createWith(ctx -> (Service) () -> "ordinary");
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClass(Consumer.class)
                .addExtension(extension).initialize()) {
            assertEquals("ordinary", container.select(Consumer.class).get().services.get().value());
        }
    }

    @Test
    public void syntheticPriorityDoesNotOverrideManagedBeanPriority() {
        Class<?> managedClass = reserve ? ManagedReserve.class : ManagedAlternative.class;
        Extension extension = new Extension() {
            void register(@Observes AfterBeanDiscovery event) {
                List<TestBean<?>> beans = new ArrayList<>();
                beans.add(new TestBean<Service>(DistinctMarker.class, Service.class, () -> () -> "B", 18, reserve));
                beans.add(new TestBean<OtherService>(managedClass, OtherService.class, () -> () -> "C", 20, reserve));
                if (reverse) {
                    Collections.reverse(beans);
                }
                beans.forEach(bean -> registerBean(event, bean));
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClasses(Consumer.class, managedClass)
                .addExtension(extension).initialize()) {
            assertEquals("B", container.select(Consumer.class).get().services.get().value());
        }
    }

    @Test
    public void reserveProducerPrioritiesArePreserved() {
        assumeTrue(reserve);
        Extension extension = new Extension() {
            void register(@Observes AfterBeanDiscovery event) {
                registerBean(event, new TestBean<Service>(DistinctMarker.class, Service.class, () -> () -> "synthetic",
                        15, true));
                registerBean(event, new TestBean<OtherService>(ReserveProducers.class, OtherService.class, () -> () -> "C",
                        30, true));
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClasses(Consumer.class, ReserveProducers.class)
                .addExtension(extension).initialize()) {
            assertEquals("field", container.select(Consumer.class).get().services.get().value());
        }
    }

    @Test
    public void syntheticPriorityUsesSameScaleAsAfterTypeDiscoveryList() {
        Class<?> low = reserve ? LowReserve.class : LowAlternative.class;
        Class<?> high = reserve ? HighReserve.class : HighAlternative.class;
        Class<?> inserted = reserve ? InsertedReserve.class : InsertedAlternative.class;
        Extension extension = new Extension() {
            void reorder(@Observes AfterTypeDiscovery event) {
                // Inserting between adjacent priorities forces Weld to rescale the list.
                List<Class<?>> enabled = reserve ? event.getReserves() : event.getAlternatives();
                enabled.add(enabled.indexOf(high), inserted);
            }

            void register(@Observes AfterBeanDiscovery event) {
                registerBean(event, new TestBean<Service>(SharedMarker.class, Service.class,
                        () -> () -> "synthetic", 15, reserve));
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClasses(Consumer.class, low, high, inserted)
                .addExtension(extension).initialize()) {
            assertEquals("synthetic", container.select(Consumer.class).get().services.get().value());
        }
    }

    @Dependent
    @Alternative
    @Priority(10)
    public static class LowAlternative implements Service {
        public String value() {
            return "managed";
        }
    }

    @Dependent
    @Alternative
    @Priority(11)
    public static class HighAlternative extends LowAlternative {
    }

    @Dependent
    @Alternative
    public static class InsertedAlternative implements Service {
        public String value() {
            return "inserted";
        }
    }

    @Dependent
    @Reserve
    @Priority(10)
    public static class LowReserve implements Service {
        public String value() {
            return "managed";
        }
    }

    @Dependent
    @Reserve
    @Priority(11)
    public static class HighReserve extends LowReserve {
    }

    @Dependent
    @Reserve
    public static class InsertedReserve implements Service {
        public String value() {
            return "inserted";
        }
    }

    @Dependent
    public static class ReserveProducers {
        @Produces
        @Reserve
        @Priority(20)
        Service field = () -> "field";

        @Produces
        @Reserve
        @Priority(10)
        Service method() {
            return () -> "method";
        }
    }

    @Dependent
    @Alternative
    @Priority(15)
    public static class ManagedAlternative implements Service {
        public String value() {
            return "managed";
        }
    }

    @Dependent
    @Reserve
    @Priority(15)
    public static class ManagedReserve implements Service {
        public String value() {
            return "managed";
        }
    }

    private void registerBean(AfterBeanDiscovery event, TestBean<?> bean) {
        if (configurator) {
            event.addBean().beanClass(bean.getBeanClass()).types(bean.getTypes())
                    .qualifiers(bean.getQualifiers()).scope(Dependent.class)
                    .alternative(bean.isAlternative()).reserve(bean.isReserve()).priority(bean.getPriority())
                    .createWith(ctx -> bean.supplier.get());
        } else {
            event.addBean(bean);
        }
    }

    private void check(boolean shared, boolean unrelated, boolean equal) {
        List<TestBean<?>> beans = new ArrayList<>();
        beans.add(new TestBean<Service>(SharedMarker.class, Service.class, () -> () -> "A", 10, reserve));
        beans.add(new TestBean<Service>(shared ? SharedMarker.class : DistinctMarker.class,
                Service.class, () -> () -> "B", equal ? 10 : 15, reserve));
        if (unrelated) {
            beans.add(new TestBean<OtherService>(SharedMarker.class, OtherService.class, () -> () -> "C", 20, reserve));
        }
        if (reverse) {
            Collections.reverse(beans);
        }
        Extension extension = new Extension() {
            void register(@Observes AfterBeanDiscovery event) {
                for (TestBean<?> bean : beans) {
                    registerBean(event, bean);
                }
            }
        };
        try (WeldContainer container = new Weld().disableDiscovery().addBeanClass(Consumer.class)
                .addExtension(extension).initialize()) {
            Instance<Service> services = container.select(Consumer.class).get().services;
            if (equal) {
                assertTrue(services.isAmbiguous());
            } else {
                assertEquals("B", services.get().value());
            }
        }
    }

    public static class Consumer {
        @Inject
        Instance<Service> services;
    }

    interface Service {
        String value();
    }

    interface OtherService {
        String value();
    }

    static class SharedMarker {
    }

    static class DistinctMarker {
    }

    static class TestBean<T> implements Bean<T>, Prioritized {
        private final Class<?> beanClass;
        private final Class<T> type;
        private final Supplier<T> supplier;
        private final int priority;
        private final boolean reserve;

        TestBean(Class<?> beanClass, Class<T> type, Supplier<T> supplier, int priority, boolean reserve) {
            this.beanClass = beanClass;
            this.type = type;
            this.supplier = supplier;
            this.priority = priority;
            this.reserve = reserve;
        }

        @Override
        public Class<?> getBeanClass() {
            return beanClass;
        }

        @Override
        public Set<Type> getTypes() {
            return Set.of(type, Object.class);
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Set.of(Default.Literal.INSTANCE, Any.Literal.INSTANCE);
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Set.of();
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Set.of();
        }

        @Override
        public boolean isAlternative() {
            return !reserve;
        }

        @Override
        public boolean isReserve() {
            return reserve;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public T create(CreationalContext<T> ctx) {
            return supplier.get();
        }

        @Override
        public void destroy(T instance, CreationalContext<T> ctx) {
            ctx.release();
        }
    }
}
