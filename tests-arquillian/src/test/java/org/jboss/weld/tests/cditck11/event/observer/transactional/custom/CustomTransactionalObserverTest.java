/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.cditck11.event.observer.transactional.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Test taken from org.jboss.cdi.tck.tests.event.observer.transactional.custom.CustomTransactionalObserverTest
 * 
 * @author Martin Kouba
 * @author Jozef Hartinger
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class CustomTransactionalObserverTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(CustomTransactionalObserverTest.class.getPackage()).addAsServiceProvider(Extension.class, ObserverExtension.class);
    }

    @Inject
    private GiraffeService giraffeService;

    @Inject
    private ObserverExtension extension;

    @Test
    public void testCustomTransactionalObserver(BeanManager manager) throws Exception {

        ActionSequence.reset();

        // GiraffeObserver 2x, GiraffeCustomObserver 1x
        assertEquals(manager.resolveObserverMethods(new Giraffe()).size(), 3);

        // Transactional invocation
        giraffeService.feed();

        // Test ObserverMethod.notify() was called
        assertNotNull(extension.getAnyGiraffeObserver().getReceivedPayload());
        // Test ObserverMethod.getTransactionPhase() was called
        assertTrue(extension.getAnyGiraffeObserver().isTransactionPhaseCalled());

        // Test custom observer received notification during the after completion phase (after succesfull commit)
        // BEFORE_COMPLETION must be fired at the beginning of the commit (after checkpoint)
        // AFTER_SUCCESS and AFTER_COMPLETION must be fired after BEFORE_COMPLETION
        // AFTER_FAILURE is not fired
        ActionSequence.getSequence().beginsWith("checkpoint", TransactionPhase.BEFORE_COMPLETION.toString());
        ActionSequence.getSequence().containsAll(TransactionPhase.AFTER_SUCCESS.toString(),
                TransactionPhase.AFTER_COMPLETION.toString());
    }

}
