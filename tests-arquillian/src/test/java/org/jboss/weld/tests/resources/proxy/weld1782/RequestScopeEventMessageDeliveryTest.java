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
package org.jboss.weld.tests.resources.proxy.weld1782;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Timer;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class RequestScopeEventMessageDeliveryTest {

    @Deployment
    public static WebArchive createTestArchive() {

        return ShrinkWrap.create(WebArchive.class, Utils.getDeploymentNameAsHash(RequestScopeEventMessageDeliveryTest.class, Utils.ARCHIVE_TYPE.WAR)).addPackage(RequestScopeEventMessageDeliveryTest.class.getPackage()).addClass(
                Timer.class);
    }

    @Inject
    private SimpleMessageProducer producer;

    @Inject
    private ApplicationScopedObserver observer;

    @Test
    public void testEventsFired() throws Exception {

        AbstractMessageListener.reset();
        observer.reset();

        producer.sendTopicMessage();

        new Timer().setDelay(5, TimeUnit.SECONDS).addStopCondition(() -> AbstractMessageListener.getProcessedMessages() >= 1).start();

        assertEquals(1, AbstractMessageListener.getProcessedMessages());
        assertTrue(AbstractMessageListener.isInitializedEventObserver());

        // wait for the request scope for the message delivery to be destroyed and verify that the event was delivered
        new Timer().setDelay(5, TimeUnit.SECONDS).addStopCondition(observer::isDestroyedCalled).start();
        assertTrue(observer.isDestroyedCalled());
    }
}
