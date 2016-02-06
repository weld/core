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
import static org.jboss.weld.test.util.ActionSequence.addAction;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessManagedBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import javax.enterprise.util.TypeLiteral;

import org.jboss.weld.environment.se.ContainerLifecycleObserver;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Test;

public class ContainerLifecyleObserverTest {

    @SuppressWarnings({ "serial" })
    @Test
    public void testExtensionBuilder() {

        ActionSequence.reset();

        Extension myExtension = ContainerLifecycleObserver.extensionBuilder()
                .add(beforeBeanDiscovery((e) -> addAction(BeforeBeanDiscovery.class.getSimpleName())))
                .add(afterTypeDiscovery().notify((e,b) -> {
                    addAction(AfterTypeDiscovery.class.getSimpleName());
                    e.addAnnotatedType(b.createAnnotatedType(Charlie.class), Charlie.class.getName());
                }))
                .add(afterBeanDiscovery((e) -> addAction(AfterBeanDiscovery.class.getSimpleName())))
                .add(afterDeploymentValidation((e) -> addAction(AfterDeploymentValidation.class.getSimpleName())))
                .add(beforeShutdown((e) -> addAction(BeforeShutdown.class.getSimpleName())))
                .build();

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
                    addAction(ProcessProducerMethod.class.getSimpleName()); } ))
                .add(processBeanAttributes().notify((e) -> addAction(ProcessBeanAttributes.class.getSimpleName())))
                .add(processObserverMethod().notify((e) -> addAction(ProcessObserverMethod.class.getSimpleName())))
                .build();

        try (WeldContainer container = new Weld()
                .disableDiscovery()
                .beanClasses(Foo.class, Bravo.class)
                .addExtension(myExtension)
                .addExtension(myExtension2)
                .initialize()) {
            assertTrue(container.select(Foo.class).isUnsatisfied());
            assertFalse(container.select(Bravo.class).isUnsatisfied());
        }

        ActionSequence.assertSequenceDataContainsAll(BeforeBeanDiscovery.class, AfterTypeDiscovery.class, AfterBeanDiscovery.class, AfterDeploymentValidation.class,
                BeforeShutdown.class);
        ActionSequence.assertSequenceDataContainsAll(ProcessBeanAttributes.class, ProcessSyntheticAnnotatedType.class, ProcessInjectionPoint.class,
                ProcessObserverMethod.class, ProcessBeanAttributes.class, ProcessProducer.class);
        ActionSequence.assertSequenceDataContainsAll(ProcessBean.class, ProcessManagedBean.class, ProcessProducerMethod.class, ProcessProducerField.class);

    }

    @Test
    public void testAddContainerLifecycleObserver() {
        final AtomicBoolean called = new AtomicBoolean(false);
        try (WeldContainer container = new Weld()
                .disableDiscovery()
                .beanClasses(Foo.class)
                .addContainerLifecycleObserver(processAnnotatedType().notify((e)-> e.veto()))
                .addContainerLifecycleObserver(afterBeanDiscovery((e)-> called.set(true)))
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
