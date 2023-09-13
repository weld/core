/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.builder.containerlifecycle;

import static org.jboss.weld.environment.se.ContainerLifecycleObserver.afterBeanDiscovery;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.afterDeploymentValidation;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.afterTypeDiscovery;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.beforeBeanDiscovery;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.beforeShutdown;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processAnnotatedType;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processBean;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processBeanAttributes;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processInjectionPoint;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processManagedBean;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processObserverMethod;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processProducer;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processProducerField;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processProducerMethod;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processSyntheticAnnotatedType;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processSyntheticBean;
import static org.jboss.weld.environment.se.ContainerLifecycleObserver.processSyntheticObserverMethod;
import static org.jboss.weld.test.util.ActionSequence.addAction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessBeanAttributes;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducer;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessSyntheticBean;
import jakarta.enterprise.inject.spi.ProcessSyntheticObserverMethod;
import jakarta.enterprise.util.TypeLiteral;

import org.jboss.weld.environment.se.ContainerLifecycleObserver;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Assert;
import org.junit.Test;

public class ContainerLifecyleObserverTest {

    @SuppressWarnings({ "serial" })
    @Test
    public void testExtensionBuilder() {

        ActionSequence.reset();

        Extension myExtension = ContainerLifecycleObserver.extensionBuilder()
                .add(beforeBeanDiscovery((e) -> addAction(BeforeBeanDiscovery.class.getSimpleName())))
                .add(afterTypeDiscovery().notify((e, b) -> {
                    addAction(AfterTypeDiscovery.class.getSimpleName());
                    e.addAnnotatedType(b.createAnnotatedType(Charlie.class), Charlie.class.getName());
                })).add(afterBeanDiscovery((e) -> {
                    addAction(AfterBeanDiscovery.class.getSimpleName());
                    e.addObserverMethod().beanClass(Foo.class).observedType(Foo.class).notifyWith((ctx) -> {
                    });
                    e.addBean().beanClass(Integer.class).addType(Integer.class).addQualifier(Juicy.Literal.INSTANCE)
                            .createWith((ctx) -> Integer.valueOf(10));
                })).add(afterDeploymentValidation((e) -> addAction(AfterDeploymentValidation.class.getSimpleName())))
                .add(beforeShutdown((e) -> addAction(BeforeShutdown.class.getSimpleName()))).build();

        Extension myExtension2 = ContainerLifecycleObserver.extensionBuilder()
                .add(processAnnotatedType().withAnnotations(RequestScoped.class).notify((e) -> e.veto()))
                .add(processBeanAttributes().notify((e) -> addAction(ProcessBeanAttributes.class.getSimpleName())))
                .add(processSyntheticAnnotatedType(new TypeLiteral<ProcessSyntheticAnnotatedType<?>>() {
                }.getType()).notify((e) -> addAction(ProcessSyntheticAnnotatedType.class.getSimpleName())))
                .add(processInjectionPoint().notify((e) -> addAction(ProcessInjectionPoint.class.getSimpleName())))
                .add(processProducer().notify((e) -> addAction(ProcessProducer.class.getSimpleName())))
                .add(processBean().notify((e) -> addAction(ProcessBean.class.getSimpleName())))
                .add(processManagedBean().notify((e) -> addAction(ProcessManagedBean.class.getSimpleName())))
                .add(processProducerField().notify((e) -> addAction(ProcessProducerField.class.getSimpleName())))
                .add(processProducerMethod().notify((e) -> {
                    // Weld SE defines some producer methods, e.g. ParametersFactory
                    addAction(ProcessProducerMethod.class.getSimpleName());
                })).add(processBeanAttributes().notify((e) -> addAction(ProcessBeanAttributes.class.getSimpleName())))
                .add(processObserverMethod().notify((e) -> addAction(ProcessObserverMethod.class.getSimpleName())))
                .add(processObserverMethod(new TypeLiteral<ProcessObserverMethod<String, ?>>() {
                }.getType())
                        .notify((e) -> addAction(ProcessObserverMethod.class.getSimpleName() + String.class.getSimpleName())))
                .add(processSyntheticObserverMethod(new TypeLiteral<ProcessSyntheticObserverMethod<Foo, ?>>() {
                }.getType()).notify(
                        (e) -> addAction(ProcessSyntheticObserverMethod.class.getSimpleName() + Foo.class.getSimpleName())))
                .add(processSyntheticBean(new TypeLiteral<ProcessSyntheticBean<Integer>>() {
                }.getType())
                        .notify((e) -> addAction(ProcessSyntheticBean.class.getSimpleName() + Integer.class.getSimpleName())))
                .build();

        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class, Bravo.class)
                .addExtension(myExtension).addExtension(myExtension2)
                .initialize()) {
            assertTrue(container.select(Foo.class).isUnsatisfied());
            assertFalse(container.select(Bravo.class).isUnsatisfied());
            Assert.assertEquals(Integer.valueOf(10), container.select(Integer.class, Juicy.Literal.INSTANCE).get());
        }

        ActionSequence.assertSequenceDataContainsAll(BeforeBeanDiscovery.class, AfterTypeDiscovery.class,
                AfterBeanDiscovery.class,
                AfterDeploymentValidation.class, BeforeShutdown.class);
        ActionSequence.assertSequenceDataContainsAll(ProcessBeanAttributes.class, ProcessSyntheticAnnotatedType.class,
                ProcessInjectionPoint.class,
                ProcessObserverMethod.class, ProcessBeanAttributes.class, ProcessProducer.class);
        ActionSequence.assertSequenceDataContainsAll(ProcessObserverMethod.class.getSimpleName() + String.class.getSimpleName(),
                ProcessSyntheticObserverMethod.class.getSimpleName() + Foo.class.getSimpleName(),
                ProcessSyntheticBean.class.getSimpleName() + Integer.class.getSimpleName());
        ActionSequence.assertSequenceDataContainsAll(ProcessBean.class, ProcessManagedBean.class, ProcessProducerMethod.class,
                ProcessProducerField.class);

    }

    @SuppressWarnings("serial")
    @Test
    public void testAddContainerLifecycleObserver() {
        final AtomicBoolean called = new AtomicBoolean(false);
        try (WeldContainer container = new Weld().disableDiscovery().beanClasses(Foo.class)
                .addContainerLifecycleObserver(processAnnotatedType(new TypeLiteral<ProcessAnnotatedType<Foo>>() {
                }.getType()).notify((e) -> e.veto())).addContainerLifecycleObserver(afterBeanDiscovery((e) -> called.set(true)))
                .initialize()) {
            assertTrue(called.get());
            assertTrue(container.select(Foo.class).isUnsatisfied());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidObservedType() {
        ContainerLifecycleObserver.processInjectionPoint(Foo.class);
    }

}
