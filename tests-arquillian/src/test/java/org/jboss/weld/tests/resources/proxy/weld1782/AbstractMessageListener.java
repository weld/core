/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class AbstractMessageListener implements MessageListener {

    private static AtomicInteger processedMessages = new AtomicInteger(0);
    private static AtomicBoolean initializedEventObserver = new AtomicBoolean();

    @Inject
    private RequestScopedObserver observer;

    @Inject
    private Controller controller;

    @Override
    public void onMessage(Message message) {

        if (message instanceof TextMessage) {
            processedMessages.incrementAndGet();
            initializedEventObserver.set(observer.isInitializedObserved());
            controller.getMsgDeliveredLatch().countDown();
        } else {
            throw new IllegalArgumentException("Unsupported message type");
        }
    }

    public static void reset() {
        processedMessages.set(0);
        initializedEventObserver.set(false);
    }

    public static int getProcessedMessages() {
        return processedMessages.get();
    }

    public static boolean isInitializedEventObserver() {
        return initializedEventObserver.get();
    }

}
