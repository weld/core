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
package org.jboss.weld.tests.event.observer.vetoreplace;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.enterprise.inject.spi.DefinitionException;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;

import org.jboss.weld.literal.NamedLiteral;

public class DummyExtension implements Extension {

    void vetoObserverMethod(@Observes ProcessObserverMethod<String, ?> event) {
        if (checkExperimentalObserver(event.getObserverMethod().getObservedQualifiers())) {
            event.veto();
        }
    }

    void replaceObserverMethod(@Observes ProcessObserverMethod<Number, ?> event) {
        if (!checkExperimentalObserver(event.getObserverMethod().getObservedQualifiers())) {
            return;
        }
        // first, verify getBeanClass() check
        ObserverMethod<Number> wrongObserver = new ForwardingObserverMethod<Number>(event.getObserverMethod()) {
            @Override
            public Class<?> getBeanClass() {
                return Number.class;
            }
        };
        try {
            event.setObserverMethod(wrongObserver);
            throw new RuntimeException("Expected exception not thrown");
        } catch (DefinitionException expected) {
        }

        ObserverMethod<Number> replacement = new ForwardingObserverMethod<Number>(event.getObserverMethod()) {

            @Override
            public Type getObservedType() {
                return Integer.class;
            }

            @Override
            public Set<Annotation> getObservedQualifiers() {
                Set<Annotation> qualifiers = new HashSet<>(delegate().getObservedQualifiers());
                qualifiers.add(new NamedLiteral("experimental"));
                return qualifiers;
            }
        };
        event.setObserverMethod(replacement);
    }

    private boolean checkExperimentalObserver(Set<Annotation> qualifiers) {
        return qualifiers.stream().filter((a) -> a.annotationType().equals(Experimental.class)).findFirst().isPresent();
    }

    private static class ForwardingObserverMethod<T> implements ObserverMethod<T> {

        private final ObserverMethod<T> delegate;

        public ForwardingObserverMethod(ObserverMethod<T> delegate) {
            this.delegate = delegate;
        }

        protected ObserverMethod<T> delegate() {
            return delegate;
        }

        @Override
        public Class<?> getBeanClass() {
            return delegate().getBeanClass();
        }

        @Override
        public Type getObservedType() {
            return delegate().getObservedType();
        }

        @Override
        public Set<Annotation> getObservedQualifiers() {
            return delegate().getObservedQualifiers();
        }

        @Override
        public Reception getReception() {
            return delegate().getReception();
        }

        @Override
        public TransactionPhase getTransactionPhase() {
            return delegate().getTransactionPhase();
        }

        @Override
        public void notify(T event) {
            delegate().notify(event);
        }
    }
}
