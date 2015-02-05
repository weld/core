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
package org.jboss.weld.tests.ejb.mdb;

import java.util.concurrent.CountDownLatch;

import javax.ejb.MessageDrivenContext;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Control {
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean messageDelivered;
    private volatile boolean contextSet;

    public boolean isMessageDelivered() {
        return messageDelivered;
    }

    public void setMessageDelivered(boolean messageDelivered) {
        this.messageDelivered = messageDelivered;
        latch.countDown();
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setContext(MessageDrivenContext context) {
        if (context != null) {
            contextSet = true;
        }
    }

    public boolean isContextSet() {
        return contextSet;
    }

}
