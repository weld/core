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
package org.jboss.weld.tests.observers.decoration;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

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
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@RunWith(Arquillian.class)
public class DecoratedObserverMethodTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(DecoratedObserverMethodTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    private Event<Job> event;

    @Inject
    PrivateWorker privateWorker;

    @Inject
    ProtectedWorker protectedWorker;

    @Inject
    PublicWorker publicWorker;

    @Inject
    PackagePrivateWorker packPrivateWorker;

    @Test
    public void testObserverUsesContextualReference() {
        ActionSequence.reset();
        // this validates that decorator is in place for all beans
        // also increments counters to 1 everywhere
        privateWorker.doStuff();
        protectedWorker.doStuff();
        publicWorker.doStuff();
        packPrivateWorker.doStuff();
        ActionSequence.assertSequenceDataContainsAll(PrivateWorker.class.getName(), ProtectedWorker.class.getName(),
                PublicWorker.class.getName(), PackagePrivateWorker.class.getName(), WorkerDecorator.class.getName());
        assertTrue(ActionSequence.getSequenceSize() == 8);
        ActionSequence.reset();

        // fire event to trigger observers
        event.fire(new Job());
        int expectedFieldValue = 1;
        ActionSequence.assertSequenceDataContainsAll(PublicWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(ProtectedWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PackagePrivateWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PrivateWorker.class.getName() + "-" + expectedFieldValue);
    }
}
