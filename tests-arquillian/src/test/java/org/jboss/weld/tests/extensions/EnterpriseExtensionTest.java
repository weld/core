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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.spi.Extension;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(Integration.class)
@RunWith(Arquillian.class)
public class EnterpriseExtensionTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(EnterpriseExtensionTest.class))
                .beanDiscoveryMode(BeanDiscoveryMode.ALL)
                .addPackage(EnterpriseExtensionTest.class.getPackage())
                .addAsServiceProvider(Extension.class,
                        SimpleExtension.class,
                        ExtensionObserver.class,
                        WoodlandExtension.class,
                        TrainlineExtension.class);
    }

    /*
    * description = "WELD-243"
    */
    @Test
    public void testContainerEventsOnlySentToExtensionBeans(ExtensionObserver extensionObserver, OtherObserver otherObserver) {
        assertTrue(extensionObserver.isBeforeBeanDiscovery());
        assertTrue(extensionObserver.isAllBeforeBeanDiscovery());
        assertFalse(otherObserver.isBeforeBeanDiscovery());
        assertFalse(otherObserver.isAllBeforeBeanDiscovery());

        assertTrue(extensionObserver.isAfterBeanDiscovery());
        assertTrue(extensionObserver.isAllAfterBeanDiscovery());
        assertFalse(otherObserver.isAfterBeanDiscovery());
        assertFalse(otherObserver.isAllAfterBeanDiscovery());

        assertTrue(extensionObserver.isProcessAnnotatedType());
        assertTrue(extensionObserver.isAllProcessAnnnotatedType());
        assertFalse(otherObserver.isProcessAnnotatedType());
        assertFalse(otherObserver.isAllProcessAnnotatedType());

        assertTrue(extensionObserver.isProcessBean());
        assertTrue(extensionObserver.isAllProcessBean());
        assertFalse(otherObserver.isProcessBean());
        assertFalse(otherObserver.isAllProcessBean());

        assertTrue(extensionObserver.isProcessInjectionTarget());
        assertTrue(extensionObserver.isAllProcessInjectionTarget());
        assertFalse(otherObserver.isProcessInjectionTarget());
        assertFalse(otherObserver.isAllProcessInjectionTarget());

        assertTrue(extensionObserver.isProcessManagedBean());
        assertTrue(extensionObserver.isAllProcessManagedBean());
        assertFalse(otherObserver.isProcessManagedBean());
        assertFalse(otherObserver.isAllProcessManagedBean());

        assertTrue(extensionObserver.isProcessObserverMethod());
        assertTrue(extensionObserver.isAllProcessObserverMethod());
        assertFalse(otherObserver.isProcessObserverMethod());
        assertFalse(otherObserver.isAllProcessObserverMethod());

        assertTrue(extensionObserver.isProcessProducer());
        assertTrue(extensionObserver.isAllProcessProducer());
        assertFalse(otherObserver.isProcessProducer());
        assertFalse(otherObserver.isAllProcessProducer());

        assertTrue(extensionObserver.isProcessProducerField());
        assertTrue(extensionObserver.isAllProcessProducerField());
        assertFalse(otherObserver.isProcessProducerField());
        assertFalse(otherObserver.isAllProcessProducerField());

        assertTrue(extensionObserver.isProcessProducerMethod());
        assertTrue(extensionObserver.isAllProcessProducerField());
        assertFalse(otherObserver.isProcessProducerMethod());
        assertFalse(otherObserver.isAllProcessProducerMethod());

        assertTrue(extensionObserver.isProcessSessionBean());
        assertTrue(extensionObserver.isAllProcessSessionBean());
        assertFalse(otherObserver.isProcessSessionBean());
        assertFalse(otherObserver.isAllProcessSessionBean());

        assertTrue(extensionObserver.isAfterDeploymentValidation());
        assertTrue(extensionObserver.isAllAfterDeploymentValidation());
        assertFalse(otherObserver.isAfterDeploymentValidation());
        assertFalse(otherObserver.isAllAfterDeploymentValidation());
    }

    /*
    * WELD-503
    */
    @Test
    public void testProcessStarOnlyCalledForEnableSessionBeans(TrainlineExtension extension) {
        assertFalse(extension.isProcessTerminusBean());
        assertFalse(extension.isProcessTerminusSessionBean());
        assertFalse(extension.isProcessTerminusInjectionTarget());
    }

}
