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
package org.jboss.weld.tests.annotatedType.annotation;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

public class SyntheticAnnotationRegisteringExtension implements Extension {

    private int eventCount;

    void registerAnnotation1(@Observes BeforeBeanDiscovery event, BeanManager manager) {
        event.addAnnotatedType(manager.createAnnotatedType(Simple.class), Simple.class.getName() + ".extension1");
    }

    void registerAnnotation2(@Observes AfterTypeDiscovery event, BeanManager manager) {
        event.addAnnotatedType(manager.createAnnotatedType(Simple.class), Simple.class.getName() + ".extension2");
    }

    void observe(@Observes ProcessAnnotatedType<Simple> event) {
        eventCount++;
    }

    int getEventCount() {
        return eventCount;
    }
}
