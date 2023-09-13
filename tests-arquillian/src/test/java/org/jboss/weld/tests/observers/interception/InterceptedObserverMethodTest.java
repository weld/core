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
package org.jboss.weld.tests.observers.interception;

import static org.junit.Assert.assertEquals;

import jakarta.enterprise.inject.spi.BeanManager;

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
public class InterceptedObserverMethodTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).addPackage(InterceptedObserverMethodTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Test
    public void testInterceptedObserver(BeanManager beanManager) {
        ActionSequence.reset();
        beanManager.getEvent().select(String.class, Juicy.Literal.INSTANCE).fire("bar");
        assertEquals(8, ActionSequence.getSequenceSize());
        ActionSequence.assertSequenceDataContainsAll(PublicObserver.class.getName(), PrivateObserver.class.getName(),
                ProtectedObserver.class.getName(),
                PackagePrivateObserver.class.getName(), SecureInterceptor.class.getName());
    }
}
