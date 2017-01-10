/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.contexts.activator.request;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class InitializedDestroyedObserver {

    private AtomicReference<Object> init;

    private AtomicReference<Object> destroy;

    @PostConstruct
    void init() {
        init = new AtomicReference<Object>(null);
        destroy = new AtomicReference<Object>(null);
    }

    public void onInit(@Observes @Initialized(RequestScoped.class) Object payload) {
        init.set(payload);
    }

    public void onDestroy(@Observes @Destroyed(RequestScoped.class) Object payload) {
        destroy.set(payload);
    }

    public Object getInit() {
        return init;
    }

    public Object getDestroy() {
        return destroy;
    }

    public void reset() {
        init.set(null);
        destroy.set(null);
    }
}
