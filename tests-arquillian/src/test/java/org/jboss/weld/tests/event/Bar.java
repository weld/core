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
package org.jboss.weld.tests.event;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;

@RequestScoped
public class Bar {

    @Inject
    Event<String> event;

    @Inject
    @Updated
    Event<String> updatedEvent;

    @Inject
    BeanManager manager;

    private boolean unqualifiedObserved;
    private boolean updatedObserved;

    public void fireWithNoQualifiers() {
        event.fire("");
    }

    public void fireWithUpdatedQualifierViaSelect() {
        event.select(new AnnotationLiteral<Updated>() {
        }).fire("");
    }

    public void fireWithNoQualifiersViaManager() {
        manager.getEvent().select(String.class).fire("");
    }

    public void fireWithUpdatedQualifierViaManager() {
        manager.getEvent().select(String.class, new AnnotationLiteral<Updated>() {
        }).fire("");
    }

    public void fireWithUpdatedQualifierViaAnnotation() {
        updatedEvent.fire("");
    }

    public void reset() {
        unqualifiedObserved = false;
        updatedObserved = false;
    }

    public void onEvent(@Observes String event) {
        unqualifiedObserved = true;
    }

    private void onUpdatedEvent(@Observes @Updated String event) {
        updatedObserved = true;
    }

    public boolean isUnqualifiedObserved() {
        return unqualifiedObserved;
    }

    public boolean isUpdatedObserved() {
        return updatedObserved;
    }

}
