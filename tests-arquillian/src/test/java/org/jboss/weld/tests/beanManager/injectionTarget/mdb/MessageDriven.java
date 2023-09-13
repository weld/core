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
package org.jboss.weld.tests.beanManager.injectionTarget.mdb;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;

@AroundConstructBinding
@Dependent
public class MessageDriven implements MessageListener {

    @Inject
    private BeanManager field;

    private final Event<Object> constructor;
    private Instance<Object> initializer;

    private boolean postConstruct;
    private boolean destroyed;

    @Inject
    public MessageDriven(Event<Object> constructor) {
        this.constructor = constructor;
    }

    @Inject
    public void init(Instance<Object> initializer) {
        this.initializer = initializer;
    }

    @PostConstruct
    void init() {
        this.postConstruct = true;
    }

    @PreDestroy
    void destroy() {
        this.destroyed = true;
    }

    @Override
    public void onMessage(Message arg0) {
    }

    @AroundInvokeBinding
    public void ping() {

    }

    public BeanManager getField() {
        return field;
    }

    public Event<Object> getConstructor() {
        return constructor;
    }

    public Instance<Object> getInitializer() {
        return initializer;
    }

    public boolean isPostConstruct() {
        return postConstruct;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

}
