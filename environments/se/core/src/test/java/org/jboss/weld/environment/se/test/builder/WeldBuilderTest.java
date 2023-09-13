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
package org.jboss.weld.environment.se.test.builder;

import static org.jboss.weld.environment.se.ContainerLifecycleObserver.afterBeanDiscovery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.event.ObserverException;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.InterceptionType;

import org.jboss.weld.Container;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.config.WeldConfiguration;
import org.jboss.weld.environment.se.ContainerLifecycleObserver;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.builder.alphas.Alpha1;
import org.jboss.weld.environment.se.test.builder.alphas.Alpha2;
import org.jboss.weld.environment.se.test.builder.alphas.betas.Beta1;
import org.jboss.weld.environment.se.test.builder.alphas.betas.Beta2;
import org.jboss.weld.manager.BeanManagerImpl;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class WeldBuilderTest {

    @Test
    public void testSyntheticBeanArchive() throws Exception {
        Weld weld = new Weld().disableDiscovery();
        try (WeldContainer container = weld.containerId("FOO").beanClasses(Foo.class, Bar.class, Cat.class).initialize()) {
            assertEquals(10, container.select(Foo.class).get().getVal());
            assertTrue(container.select(Bar.class).isUnsatisfied());
            assertTrue(container.select(Cat.class).isUnsatisfied());
            assertTrue(container.select(Qux.class).isUnsatisfied());
        }
        // Test AutoCloseable
        assertNull(WeldContainer.instance("FOO"));
        // Test alternatives selected for the synthetic BDA
        try (WeldContainer container = weld.beanClasses(Foo.class, Bar.class, Cat.class).alternatives(Bar.class)
                .alternativeStereotypes(AlternativeStereotype.class).initialize()) {
            assertEquals(10, container.select(Foo.class).get().getVal());
            assertEquals(1, container.select(Bar.class).get().getVal());
            assertEquals(5, container.select(Cat.class).get().getVal());
        }
        // Test interceptor enabled for the synthetic BDA
        try (WeldContainer container = weld.reset().beanClasses(Qux.class, MonitoringInterceptor.class)
                .interceptors(MonitoringInterceptor.class)
                .initialize()) {
            assertEquals(Integer.valueOf(11), container.select(Qux.class).get().ping());
        }
        // Test decorator enabled for the synthetic BDA
        try (WeldContainer container = weld.reset().beanClasses(Foo.class, CoolDecorator.class).decorators(CoolDecorator.class)
                .initialize()) {
            assertEquals("NOK", container.select(Foo.class).get().methodToBeDecorated());
        }
        // Test addBeanClass()
        try (WeldContainer container = weld.reset().beanClasses(Bar.class).addBeanClass(Foo.class).alternatives(Bar.class)
                .initialize()) {
            assertEquals(10, container.select(Foo.class).get().getVal());
            assertEquals(1, container.select(Bar.class).get().getVal());
            assertTrue(container.select(Baz.class).isUnsatisfied());
        }
    }

    @Test
    public void testMultipleWeldInstancesCreated() {
        Baz.DESTROYED.clear();
        Weld weld = new Weld().disableDiscovery();
        int loop = 5;
        List<WeldContainer> containers = new ArrayList<WeldContainer>();
        for (int i = 0; i < loop; i++) {
            containers.add(weld.containerId("" + i).beanClasses(Baz.class).initialize());
            WeldContainer.getRunningContainerIds().contains(String.valueOf(i));
            assertTrue(WeldContainer.getRunningContainerIds().size() == i + 1);
        }
        for (WeldContainer container : containers) {
            assertTrue(container.isRunning());
            assertEquals(container.getId(), container.select(Baz.class).get().getVal());
        }
        weld.shutdown();
        assertEquals(loop, Baz.DESTROYED.size());
        for (int i = 0; i < loop; i++) {
            assertTrue(Baz.DESTROYED.contains("" + i));
        }
        for (WeldContainer container : containers) {
            assertFalse(container.isRunning());
        }
    }

    @Test
    public void testConfigurationProperties() {
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class)
                .property(ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), false)
                .initialize()) {
            assertFalse(container.select(BeanManagerImpl.class).get().getServices().get(WeldConfiguration.class)
                    .getBooleanProperty(ConfigurationKey.CONCURRENT_DEPLOYMENT));
        }
    }

    @Test
    public void testReset() {
        Weld weld = new Weld().containerId("FOO").disableDiscovery()
                .property(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), true)
                .beanClasses(Foo.class);
        weld.reset();
        assertFalse(weld.isDiscoveryEnabled());
        assertEquals("FOO", weld.getContainerId());
        try (WeldContainer container = weld.beanClasses(Bar.class).initialize()) {
            assertTrue(container.select(Foo.class).isUnsatisfied());
            assertTrue(container.select(BeanManagerImpl.class).get().getServices().get(WeldConfiguration.class)
                    .getBooleanProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION));
        }
    }

    @Test
    public void testResetAll() {
        Weld weld = new Weld().containerId("FOO").disableDiscovery()
                .property(ConfigurationKey.RELAXED_CONSTRUCTION.get(), false).beanClasses(Foo.class);
        weld.resetAll();
        assertTrue(weld.isDiscoveryEnabled());
        assertNull(weld.getContainerId());
        weld.disableDiscovery();
        try (WeldContainer container = weld.beanClasses(Bar.class).initialize()) {
            assertTrue(container.select(Foo.class).isUnsatisfied());
            assertTrue(container.select(BeanManagerImpl.class).get().getServices().get(WeldConfiguration.class)
                    .getBooleanProperty(ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION));
        }
    }

    @Test
    public void testLifecycle() {
        Weld weld = new Weld().disableDiscovery().beanClasses(Foo.class, DependentFoo.class);
        try (WeldContainer container = weld.initialize()) {
            container.select(DependentFoo.class).get().getVal();
        }
        assertTrue(DependentFoo.destroyCallbackCalled.get());
        DependentFoo.reset();
        try (WeldContainer container = weld.initialize()) {
            DependentFoo dependentFoo = container.select(DependentFoo.class).get();
            dependentFoo.getVal();
            container.destroy(dependentFoo);
            assertTrue(DependentFoo.destroyCallbackCalled.get());
        }
    }

    @Test
    public void testSyntheticBeanArchivePackages() throws Exception {
        Weld weld = new Weld().disableDiscovery();
        try (WeldContainer container = weld.packages(Alpha1.class).initialize()) {
            assertEquals(1, container.select(Alpha1.class).get().getVal());
            assertEquals(2, container.select(Alpha2.class).get().getVal());
            assertTrue(container.select(Beta1.class).isUnsatisfied());
            assertTrue(container.select(Beta2.class).isUnsatisfied());
        }
        try (WeldContainer container = weld.reset().addPackage(true, Alpha2.class).initialize()) {
            assertEquals(1, container.select(Alpha1.class).get().getVal());
            assertEquals(2, container.select(Alpha2.class).get().getVal());
            assertEquals(11, container.select(Beta1.class).get().getVal());
            assertEquals(22, container.select(Beta2.class).get().getVal());
        }
        try (WeldContainer container = weld.reset().addPackages(true, Beta1.class).initialize()) {
            assertTrue(container.select(Alpha1.class).isUnsatisfied());
            assertTrue(container.select(Alpha2.class).isUnsatisfied());
            assertEquals(11, container.select(Beta1.class).get().getVal());
            assertEquals(22, container.select(Beta2.class).get().getVal());
        }
        // Scan the package from cdi-api.jar, use discovery mode ALL
        try (WeldContainer container = weld.reset().packages(ObserverException.class)
                .setBeanDiscoveryMode(BeanDiscoveryMode.ALL).initialize()) {
            assertFalse(container.select(ObserverException.class).isUnsatisfied());
        }
        // Scan the package recursively from cdi-api.jar
        try (WeldContainer container = weld.reset().addPackage(true, Any.class).initialize()) {
            // There is no managed bean discovered, therefore we only check that the bean class was found
            boolean found = false;
            for (BeanDeploymentArchive beanDeploymentArchive : Container.instance(container.getId()).beanDeploymentArchives()
                    .keySet()) {
                if (beanDeploymentArchive.getBeanClasses().contains(DefinitionException.class.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }

    @Test
    public void testExtensions() {
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Bar.class).extensions(new TestExtension())
                .initialize()) {
            assertEquals(10, container.select(Foo.class).get().getVal());
            assertFalse(container.select(TestExtension.class).isUnsatisfied());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNoBeanArchivesFound() {
        new Weld().disableDiscovery().initialize();
    }

    @Test
    public void testAccessibility() {
        try (WeldContainer container = new Weld().beanClasses(Oof.class).extensions(new OofExtension()).initialize()) {
            assertEquals(10, container.select(Oof.class).get().getVal());
        }
    }

    @Test
    public void testBeanBuilder() {
        try (WeldContainer container = new Weld().disableDiscovery()
                .addContainerLifecycleObserver(
                        afterBeanDiscovery((e) -> e.addBean().addType(Integer.class).produceWith((i) -> 42)
                                .addQualifier(Juicy.Literal.INSTANCE)))
                .initialize()) {
            assertEquals(Integer.valueOf(42), container.select(Integer.class, Juicy.Literal.INSTANCE).get());
        }
    }

    @Test
    public void testInterceptorBuilder() {
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Coorge.class, BuilderInterceptorBinding.class)
                .addContainerLifecycleObserver(ContainerLifecycleObserver
                        .afterBeanDiscovery((e) -> e.addInterceptor()
                                .addBinding(new BuilderInterceptorBinding.BuilderInterceptorBindingLiteral())
                                .priority(2500).intercept(InterceptionType.AROUND_INVOKE, invocationContext -> {
                                    try {
                                        Integer result = ((Integer) invocationContext.proceed());
                                        return result + 10;
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }
                                    return null;
                                })))
                .initialize()) {
            Coorge coorge = container.select(Coorge.class).get();
            assertEquals(coorge.ping(), 11);
        }
    }

}
