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
package org.jboss.weld.tests.contexts.conversation.timeout.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StateHolder {

    private static final Logger logger = Logger.getLogger(StateHolder.class.getName());

    private AtomicBoolean isBusyAttemptMade = new AtomicBoolean(false);

    public void setBusyAttemptMade() {
        isBusyAttemptMade.set(true);
        logger.info("Busy conversation attempt made");
    }

    public boolean isBusyAttemptMade() {
        return isBusyAttemptMade.get();
    }

    public void reset() {
        isBusyAttemptMade.set(false);
    }
}
