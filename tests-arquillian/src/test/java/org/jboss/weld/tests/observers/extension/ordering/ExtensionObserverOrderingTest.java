/*
 * JBoss, Home of Professional Open Source
 * Copyright 2019, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.observers.extension.ordering;

import jakarta.enterprise.inject.spi.Extension;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test manifests behaviour described in WELD-2568.
 * Note that running this with -Dincontainer means Jandex will be used whereas running it without WFLY means no Jandex.
 */
@RunWith(Arquillian.class)
public class ExtensionObserverOrderingTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(ExtensionObserverOrderingTest.class.getPackage())
                .addClass(ActionSequence.class)
                .addAsServiceProvider(Extension.class, UfoBelieverExtension.class, UfoDisbelieverExtension.class);
    }

    @Test
    public void testObserversAreOrdered() {
        ActionSequence.getSequenceData();
        Assert.assertTrue(ActionSequence.getSequenceData() != null);
        Assert.assertEquals(UfoDisbelieverExtension.class.getSimpleName(), ActionSequence.getSequenceData().get(0));
        Assert.assertEquals(UfoBelieverExtension.class.getSimpleName(), ActionSequence.getSequenceData().get(1));
    }
}
