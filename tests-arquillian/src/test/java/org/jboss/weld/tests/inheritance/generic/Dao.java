/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.inheritance.generic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

/**
 * @author Martin Kouba
 */
@Vetoed
public class Dao<T extends Entity> {

    @Inject
    T field;
    T initializerParameter;
    T event;
    T observerInjectionPoint;

    @Inject
    public void init(T parameter) {
        this.initializerParameter = parameter;
    }

    public void observeBaz(@Observes T event, T injectionPoint) {
        assertNull(this.event);
        assertNull(this.observerInjectionPoint);
        assertNotNull(event);
        assertNotNull(injectionPoint);
        this.event = event;
        this.observerInjectionPoint = injectionPoint;
    }

    public T getField() {
        return field;
    }

    public T getInitializerParameter() {
        return initializerParameter;
    }

    public T getEvent() {
        return event;
    }

    public T getObserverInjectionPoint() {
        return observerInjectionPoint;
    }
}
