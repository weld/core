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
package org.jboss.weld.tests.extensions.lifecycle.atd.prioritized;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Martin Kouba
 * @see WELD-2000
 */
@RunWith(Arquillian.class)
public class AfterTypeDiscoveryWithPrioritizedCustomBeanTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class)
                .addPackage(AfterTypeDiscoveryWithPrioritizedCustomBeanTest.class.getPackage())
                .addClasses(ActionSequence.class).addAsServiceProvider(Extension.class, TestExtension.class);
    }

    @Test
    public void testFinalInterceptors(TestExtension extension, MonitoredService monitoredService) {
        assertEquals(Arrays.asList(BravoInterceptor.class, AlphaInterceptor.class), extension.getInitialInterceptors());
        assertNotNull(monitoredService);
        ActionSequence.reset();
        monitoredService.ping();
        ActionSequence.assertSequenceDataEquals(PrioritizedInterceptor.class.getName(), CharlieInterceptor.class.getName(),
                AlphaInterceptor.class.getName(), LegacyPrioritizedInterceptor.class.getName());
    }

}
