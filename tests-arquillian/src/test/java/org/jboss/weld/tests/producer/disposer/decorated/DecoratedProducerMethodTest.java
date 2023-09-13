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
package org.jboss.weld.tests.producer.disposer.decorated;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
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
public class DecoratedProducerMethodTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(DecoratedProducerMethodTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    @Any
    Instance<PrivateWorker> privateWorker;

    @Inject
    @Any
    Instance<ProtectedWorker> protectedWorker;

    @Inject
    @Any
    Instance<PublicWorker> publicWorker;

    @Inject
    @Any
    Instance<PackagePrivateWorker> packPrivateWorker;

    @Test
    public void testProducerWithDecorator(BeanManager manager) {
        ActionSequence.reset();
        // instances with @Default, increments counters
        privateWorker.select(Default.Literal.INSTANCE).get().doStuff();
        protectedWorker.select(Default.Literal.INSTANCE).get().doStuff();
        publicWorker.select(Default.Literal.INSTANCE).get().doStuff();
        packPrivateWorker.select(Default.Literal.INSTANCE).get().doStuff();

        // verify decorator invocation
        ActionSequence.assertSequenceDataContainsAll(PrivateWorker.class.getName(), ProtectedWorker.class.getName(),
                PublicWorker.class.getName(), PackagePrivateWorker.class.getName(), WorkerDecorator.class.getName());
        assertEquals(8, ActionSequence.getSequenceSize());
        ActionSequence.reset();

        // force creation of beans with @Lazy
        PrivateWorker lazyPrivate = privateWorker.select(Lazy.Literal.INSTANCE).get();
        lazyPrivate.doStuff();
        ProtectedWorker lazyProtected = protectedWorker.select(Lazy.Literal.INSTANCE).get();
        lazyProtected.doStuff();
        PublicWorker lazyPublic = publicWorker.select(Lazy.Literal.INSTANCE).get();
        lazyPublic.doStuff();
        PackagePrivateWorker lazyPackPrivate = packPrivateWorker.select(Lazy.Literal.INSTANCE).get();
        lazyPackPrivate.doStuff();

        // assert expected results, beans created with @Lazy should already see counter == 1
        int expectedFieldValue = 1;
        assertEquals(8, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll(PublicWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(ProtectedWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PackagePrivateWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PrivateWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.reset();

        // invoke disposer methods and assert
        privateWorker.destroy(lazyPrivate);
        protectedWorker.destroy(lazyProtected);
        publicWorker.destroy(lazyPublic);
        packPrivateWorker.destroy(lazyPackPrivate);

        assertTrue(ActionSequence.getSequenceSize() == 4);
        ActionSequence.assertSequenceDataContainsAll(PublicWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(ProtectedWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PackagePrivateWorker.class.getName() + "-" + expectedFieldValue);
        ActionSequence.assertSequenceDataContainsAll(PrivateWorker.class.getName() + "-" + expectedFieldValue);

    }
}
