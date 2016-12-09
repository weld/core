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
package org.jboss.weld.tests.observers.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ObserverMethod;

public class CustomObserverExtension implements Extension {

    public <T> void registerObservers(@Observes AfterBeanDiscovery event) {
        event.addObserverMethod(new ObserverMethod<Long>() {

            @Override
            public Class<?> getBeanClass() {
                return CustomObserverExtension.class;
            }

            @Override
            public Type getObservedType() {
                return Long.class;
            }

            @Override
            public Set<Annotation> getObservedQualifiers() {
                return Collections.emptySet();
            }

            @Override
            public Reception getReception() {
                return Reception.ALWAYS;
            }

            @Override
            public TransactionPhase getTransactionPhase() {
                return TransactionPhase.IN_PROGRESS;
            }
        });
    }

}
