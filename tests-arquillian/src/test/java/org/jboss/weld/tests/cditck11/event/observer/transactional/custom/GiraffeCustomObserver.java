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
package org.jboss.weld.tests.cditck11.event.observer.transactional.custom;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

public class GiraffeCustomObserver implements ObserverMethod<Giraffe> {

    private boolean transactionPhaseCalled;
    private Giraffe receivedPayload;
    private Set<Annotation> receivedQualifiers;

    private final Set<Annotation> qualifiers;

    protected GiraffeCustomObserver(Set<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
        reset();
    }

    protected GiraffeCustomObserver(Annotation... qualifiers) {
        this(new HashSet<Annotation>(Arrays.asList(qualifiers)));
    }

    protected void reset() {
        this.receivedPayload = null;
        this.receivedQualifiers = null;
        this.transactionPhaseCalled = false;
    }

    public Class<?> getBeanClass() {
        return this.getClass();
    }

    public Type getObservedType() {
        return Giraffe.class;
    }

    public Set<Annotation> getObservedQualifiers() {
        return qualifiers;
    }

    public Reception getReception() {
        return Reception.ALWAYS;
    }

    public TransactionPhase getTransactionPhase() {
        this.transactionPhaseCalled = true;
        return TransactionPhase.AFTER_SUCCESS;
    }

    public void notify(Giraffe event) {
        ActionSequence.addAction(TransactionPhase.AFTER_SUCCESS.toString());
        receivedPayload = event;
        receivedQualifiers = qualifiers;
    }

    public Giraffe getReceivedPayload() {
        return receivedPayload;
    }

    public Set<Annotation> getReceivedQualifiers() {
        return receivedQualifiers;
    }

    public boolean isTransactionPhaseCalled() {
        return transactionPhaseCalled;
    }

}