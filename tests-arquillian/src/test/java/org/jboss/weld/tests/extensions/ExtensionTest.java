/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.extensions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ExtensionTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ExtensionTest.class))
                .addPackage(ExtensionTest.class.getPackage())
                .addAsServiceProvider(Extension.class,
                        SimpleExtension.class,
                        ExtensionObserver.class,
                        WoodlandExtension.class,
                        TrainlineExtension.class);
    }

    /*
     * description = "WELD-234"
     */
    @Test
    public void testExtensionInjectableAsBean(SimpleExtension extension) {
        assertTrue(extension.isObservedBeforeBeanDiscovery());
    }

    /*
     * description = "WELD-572"
     */
    @Test
    public void testGetNonExistentDisposalMethod(ExtensionObserver extensionObserver) {
        assertNull(extensionObserver.getProducerMethodDisposerParameter());
    }

    @Test
    public void testInjectionTargetWrapped(Capercaillie capercaillie) {
        assertTrue(Woodland.isPostConstructCalled());
        assertTrue(WoodlandExtension.isInjectCalled());
        assertTrue(WoodlandExtension.isPostConstructCalled());
        assertTrue(WoodlandExtension.isPreDestroyCalled());
        assertTrue(WoodlandExtension.isProduceCalled());
    }

    /*
     * WELD-503
     */
    @Test
    public void testProcessStarOnlyCalledForEnabledManagedBeans(TrainlineExtension extension) {
        assertTrue(extension.isProcessTrainBean());
        assertFalse(extension.isProcessStationBean());
        assertFalse(extension.isProcessSignalBoxBean());
        assertTrue(extension.isProcessTrainManagedBean());
        assertFalse(extension.isProcessStationManagedBean());
        assertFalse(extension.isProcessSignalBoxManagedBean());
        assertTrue(extension.isProcessTrainInjectionTarget());
        assertFalse(extension.isProcessStationInjectionTarget());
        assertFalse(extension.isProcessSignalBoxInjectionTarget());
    }

    /*
     * WELD-503
     */
    @Test
    public void testProcessStarOnlyCalledForEnabledProducerMethods(TrainlineExtension extension) {
        assertTrue(extension.isProcessDriverBean());
        assertFalse(extension.isProcessPassengerBean());
        assertFalse(extension.isProcessSignalManBean());
        assertTrue(extension.isProcessDriverProducerMethod());
        assertFalse(extension.isProcessPassengerProducerMethod());
        assertFalse(extension.isProcessSignalManProducerMethod());
        assertTrue(extension.isProcessDriverProducer());
        assertFalse(extension.isProcessPassengerProducer());
        assertFalse(extension.isProcessSignalManProducer());

        assertFalse(extension.isProcessStokerBean());
        assertFalse(extension.isProcessGuardBean());
        assertFalse(extension.isProcessStokerProducerMethod());
        assertFalse(extension.isProcessGuardProducerMethod());
        assertFalse(extension.isProcessStokerProducer());
        assertFalse(extension.isProcessGuardProducer());
    }

    /*
     * WELD-503
     */
    @Test
    public void testProcessStarOnlyCalledForEnabledProducerFields(TrainlineExtension extension) {
        assertTrue(extension.isProcessFerretBean());
        assertFalse(extension.isProcessCatBean());
        assertFalse(extension.isProcessMouseBean());
        assertTrue(extension.isProcessFerretProducerField());
        assertFalse(extension.isProcessCatProducerField());
        assertFalse(extension.isProcessMouseProducerField());
        assertTrue(extension.isProcessFerretProducer());
        assertFalse(extension.isProcessCatProducer());
        assertFalse(extension.isProcessMouseProducer());

        assertFalse(extension.isProcessRabbitBean());
        assertFalse(extension.isProcessWeaselBean());
        assertFalse(extension.isProcessRabbitProducerField());
        assertFalse(extension.isProcessWeaselProducerField());
        assertFalse(extension.isProcessRabbitProducer());
        assertFalse(extension.isProcessWeaselProducer());
    }

    /*
     * WELD-503
     */
    @Test
    public void testProcessStarOnlyCalledForEnabledObserverMethods(TrainlineExtension extension) {
        assertTrue(extension.isProcessObseversCoalSupply());
        assertFalse(extension.isProcessObseversFatController());
        assertFalse(extension.isProcessObseversSignals());
    }

    /*
     * WELD-503
     */
    @Test
    public void testProcessBeanOnlyCalledForEnabledInterceptorsAndDecorators(TrainlineExtension extension) {
        assertFalse(extension.isProcessSafetyInterceptor());
        assertFalse(extension.isProcessEngineeringWorks());
    }

}
