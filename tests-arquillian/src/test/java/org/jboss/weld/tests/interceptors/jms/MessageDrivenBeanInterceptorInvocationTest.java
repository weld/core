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
package org.jboss.weld.tests.interceptors.jms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

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
 * Test that invocations of message listener methods of message-driven beans during message delivery are business method invocations.
 *
 * Note that a basic JMS configuration is required for this test.
 *
 * @author Martin Kouba
 * @author Tomas Remes
 */
@Category(Integration.class)
@RunWith(Arquillian.class)
public class MessageDrivenBeanInterceptorInvocationTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class).intercept(MissileInterceptor.class)
                .addPackage(MessageDrivenBeanInterceptorInvocationTest.class.getPackage());
    }

    @Inject
    SimpleMessageProducer producer;

    @Test
    public void testMessageDrivenBeanMethodIntercepted() throws Exception {
        producer.sendQueueMessage();
        assertEquals(MessageDrivenMissile.class.getName(), MessageDrivenMissile.MESSAGES.poll(5, TimeUnit.SECONDS));
        assertTrue(MissileInterceptor.INTERCEPTED.get());
    }

}