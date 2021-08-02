/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.security.members;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;

@Dependent
class SimpleBean implements Simple {

    static Double getDouble(BeanManager manager) {
        return 0d;
    }

    @Inject
    private BeanManager manager;

    @Produces
    private Float f = 0f;

    SimpleBean() {
    }

    @Inject
    SimpleBean(Event<String> event) {
    }

    @Inject
    protected void init(Instance<Object> instance) {
    }

    @Produces
    Integer produceInteger(BeanManager manager, Conversation conversation) {
        return 0;
    }

    void disposeInteger(@Disposes Number number) {
    }

    void observeString(@Observes String string, BeanManager manager) {
    }

    @Override
    public void ping() {
    }
}
