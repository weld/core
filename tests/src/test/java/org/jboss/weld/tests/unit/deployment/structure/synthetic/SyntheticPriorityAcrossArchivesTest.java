/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.weld.tests.unit.deployment.structure.synthetic;

import static org.jboss.weld.test.util.Utils.getReference;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Vetoed;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Prioritized;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.weld.embedded.mock.BeanDeploymentArchiveImpl;
import org.jboss.arquillian.container.weld.embedded.mock.FlatDeployment;
import org.jboss.arquillian.container.weld.embedded.mock.TestContainer;
import org.jboss.weld.bootstrap.event.WeldAfterBeanDiscovery;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Extends the WELD-2000 custom priority and WELD-2658 lifecycle-event coverage to
 * multiple bean deployment archives.
 */
public class SyntheticPriorityAcrossArchivesTest {

    @DataProvider
    public Object[][] priorities() {
        return new Object[][] { { false }, { true } };
    }

    @Test(dataProvider = "priorities")
    public void testSyntheticPriorityAcrossArchives(boolean configurator) {
        PriorityExtension extension = new PriorityExtension(configurator);
        BeanDeploymentArchiveImpl owner = new BeanDeploymentArchiveImpl("owner", Marker.class, PriorityExtension.class);
        BeanDeploymentArchiveImpl first = new BeanDeploymentArchiveImpl("first-client", FirstConsumer.class);
        BeanDeploymentArchiveImpl second = new BeanDeploymentArchiveImpl("second-client", SecondConsumer.class);
        BeanDeploymentArchiveImpl isolated = new BeanDeploymentArchiveImpl("isolated", IsolatedMarker.class);
        first.getBeanDeploymentArchives().add(owner);
        second.getBeanDeploymentArchives().add(owner);

        Deployment deployment = new FlatDeployment(new BeanDeploymentArchive[] { owner, first, second, isolated }, extension) {
            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                if (beanClass == FirstConsumer.class) {
                    return first;
                }
                if (beanClass == SecondConsumer.class) {
                    return second;
                }
                if (beanClass == IsolatedMarker.class) {
                    return isolated;
                }
                return owner;
            }

            @Override
            public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
                return loadBeanDeploymentArchive(beanClass);
            }
        };

        TestContainer container = new TestContainer(deployment);
        try {
            container.startContainer();
            BeanManager ownerManager = container.getBeanManager(owner);
            BeanManager firstManager = container.getBeanManager(first);
            BeanManager secondManager = container.getBeanManager(second);
            BeanManager isolatedManager = container.getBeanManager(isolated);
            assertNotSame(ownerManager, firstManager);
            assertNotSame(ownerManager, secondManager);
            assertNotSame(firstManager, secondManager);
            for (BeanManager manager : new BeanManager[] { ownerManager, firstManager, secondManager }) {
                assertEquals(manager.getBeans(Service.class).size(), 2);
                assertEquals(getReference(manager, Service.class).value(), "higher");
            }
            // Direct injection also checks enablement during deployment validation.
            assertEquals(getReference(firstManager, FirstConsumer.class).service.value(), "higher");
            assertEquals(getReference(secondManager, SecondConsumer.class).service.value(), "higher");
            assertTrue(isolatedManager.getBeans(Service.class).isEmpty());
            assertEquals(extension.processBeanEvents, 2);
            assertEquals(extension.processSyntheticBeanEvents, 2);
        } finally {
            container.stopContainer();
        }
    }

    @Test
    public void testArchiveCreatedDuringAfterBeanDiscovery() {
        BeanDeploymentArchiveImpl owner = new BeanDeploymentArchiveImpl("owner", ManagedAlternative.class);
        BeanDeploymentArchiveImpl late = new BeanDeploymentArchiveImpl("late", Marker.class);
        late.getBeanDeploymentArchives().add(owner);
        Extension extension = new Extension() {
            void register(@Observes WeldAfterBeanDiscovery event) {
                event.addBean(new TestBean<Service>(Marker.class, Service.class, () -> () -> "synthetic", 20));
            }
        };
        Deployment deployment = new FlatDeployment(owner, extension) {
            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                if (beanClass == Marker.class) {
                    owner.getBeanDeploymentArchives().add(late);
                    return late;
                }
                return owner;
            }

            @Override
            public BeanDeploymentArchive getBeanDeploymentArchive(Class<?> beanClass) {
                return loadBeanDeploymentArchive(beanClass);
            }
        };
        TestContainer container = new TestContainer(deployment);
        try {
            container.startContainer();
            BeanManager ownerManager = container.getBeanManager(owner);
            BeanManager lateManager = container.getBeanManager(late);
            assertNotSame(ownerManager, lateManager);
            assertEquals(getReference(lateManager, Service.class).value(), "synthetic");
            assertEquals(lateManager.getBeans(ManagedAlternative.class).size(), 1);
        } finally {
            container.stopContainer();
        }
    }

    @Dependent
    @Alternative
    @Priority(10)
    public static class ManagedAlternative {
    }

    public static class PriorityExtension implements Extension {
        private final boolean configurator;
        int processBeanEvents;
        int processSyntheticBeanEvents;

        PriorityExtension(boolean configurator) {
            this.configurator = configurator;
        }

        void register(@Observes WeldAfterBeanDiscovery event) {
            register(event, 10, "lower");
            register(event, 20, "higher");
        }

        private void register(WeldAfterBeanDiscovery event, int priority, String value) {
            if (configurator) {
                event.addBean().beanClass(Marker.class).types(Service.class, Object.class)
                        .qualifiers(Default.Literal.INSTANCE, Any.Literal.INSTANCE).scope(Dependent.class)
                        .alternative(true).priority(priority)
                        .createWith(ctx -> (Service) () -> value);
            } else {
                event.addBean(new TestBean<Service>(Marker.class, Service.class, () -> () -> value, priority));
            }
        }

        void processBean(@Observes ProcessBean<?> event) {
            if (event.getBean().getTypes().contains(Service.class)) {
                processBeanEvents++;
            }
        }

        void processSyntheticBean(@Observes ProcessSyntheticBean<?> event) {
            if (event.getBean().getTypes().contains(Service.class)) {
                processSyntheticBeanEvents++;
            }
        }
    }

    public interface Service {
        String value();
    }

    @Dependent
    public static class FirstConsumer {
        @Inject
        Service service;
    }

    @Dependent
    public static class SecondConsumer {
        @Inject
        Service service;
    }

    @Vetoed
    public static class Marker {
    }

    @Vetoed
    public static class IsolatedMarker {
    }

    static class TestBean<T> implements Bean<T>, Prioritized {
        private final Class<?> beanClass;
        private final Class<T> type;
        private final Supplier<T> supplier;
        private final int priority;

        TestBean(Class<?> beanClass, Class<T> type, Supplier<T> supplier, int priority) {
            this.beanClass = beanClass;
            this.type = type;
            this.supplier = supplier;
            this.priority = priority;
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
            return true;
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
