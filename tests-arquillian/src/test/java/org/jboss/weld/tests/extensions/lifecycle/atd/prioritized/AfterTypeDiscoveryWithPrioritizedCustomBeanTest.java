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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import javax.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
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
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(AfterTypeDiscoveryWithPrioritizedCustomBeanTest.class))
                .addPackage(AfterTypeDiscoveryWithPrioritizedCustomBeanTest.class.getPackage()).addClasses(ActionSequence.class)
                .addAsServiceProvider(Extension.class, TestExtension.class);
    }

    @Test
    public void testFinalInterceptors(TestExtension extension, MonitoredService monitoredService) {
        assertTrue(extension.getInitialInterceptors().contains(BravoInterceptor.class));
        assertTrue(extension.getInitialInterceptors().contains(AlphaInterceptor.class));
        assertIndexLessThan(extension.getInitialInterceptors(), BravoInterceptor.class, AlphaInterceptor.class);
        assertNotNull(monitoredService);
        ActionSequence.reset();
        monitoredService.ping();
        ActionSequence.assertSequenceDataContainsAll(PrioritizedInterceptor.class.getName(), CharlieInterceptor.class.getName(),
                AlphaInterceptor.class.getName(), LegacyPrioritizedInterceptor.class.getName());
        List<String> data = ActionSequence.getSequenceData();
        assertIndexLessThan(data, PrioritizedInterceptor.class.getName(), CharlieInterceptor.class.getName());
        assertIndexLessThan(data, CharlieInterceptor.class.getName(), AlphaInterceptor.class.getName());
        assertIndexLessThan(data, AlphaInterceptor.class.getName(), LegacyPrioritizedInterceptor.class.getName());
    }

    private <T> void assertIndexLessThan(List<T> list, T arg1, T arg2) {
        int idx1 = getIndex(list, arg1);
        int idx2 = getIndex(list, arg2);
        if (idx1 > idx2) {
            fail(String.format("Index %s of %s is not less than %s of %s. List: %s", idx1, arg1, idx2, arg2, list));
        }
    }

    private <T> int getIndex(List<T> list, T clazz) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(clazz)) {
                return i;
            }
        }
        throw new AssertionError(clazz + " not in the list: " + list);
    }

}
