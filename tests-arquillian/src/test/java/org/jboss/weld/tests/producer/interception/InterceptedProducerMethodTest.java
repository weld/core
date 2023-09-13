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
package org.jboss.weld.tests.producer.interception;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
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
 * @author Martin Kouba
 *
 */
@RunWith(Arquillian.class)
public class InterceptedProducerMethodTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(InterceptedProducerMethodTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    @Any
    Instance<String> instance;

    @Test
    public void testPrivateProducer() {
        ActionSequence.reset();
        String product = instance.select(Alpha.Literal.INSTANCE).get();
        assertEquals(PrivateProducer.class.getName(), product);
        instance.destroy(product);
        assertEquals(3, ActionSequence.getSequenceSize());
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(0));
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(1));
        assertEquals(PrivateProducer.class.getName(), ActionSequence.getSequenceData().get(2));
    }

    @Test
    public void testProtectedProducer() {
        ActionSequence.reset();
        String product = instance.select(Bravo.Literal.INSTANCE).get();
        assertEquals(ProtectedProducer.class.getName(), product);
        instance.destroy(product);
        assertEquals(3, ActionSequence.getSequenceSize());
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(0));
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(1));
        assertEquals(ProtectedProducer.class.getName(), ActionSequence.getSequenceData().get(2));
    }

    @Test
    public void testPackagePrivateProducer() {
        ActionSequence.reset();
        String product = instance.select(Charlie.Literal.INSTANCE).get();
        assertEquals(PackagePrivateProducer.class.getName(), product);
        instance.destroy(product);
        assertEquals(3, ActionSequence.getSequenceSize());
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(0));
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(1));
        assertEquals(PackagePrivateProducer.class.getName(), ActionSequence.getSequenceData().get(2));
    }

    @Test
    public void testPublicProducer() {
        ActionSequence.reset();
        String product = instance.select(Delta.Literal.INSTANCE).get();
        assertEquals(PublicProducer.class.getName(), product);
        instance.destroy(product);
        assertEquals(3, ActionSequence.getSequenceSize());
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(0));
        assertEquals(SecureInterceptor.class.getName(), ActionSequence.getSequenceData().get(1));
        assertEquals(PublicProducer.class.getName(), ActionSequence.getSequenceData().get(2));
    }
}
