/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.weld.tests.observers.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

public class GiraffeObserver implements ObserverMethod<Giraffe> {

    private boolean legacyNotifyCalled;
    private Giraffe receivedPayload;
    private Set<Annotation> receivedQualifiers;

    private final Set<Annotation> qualifiers;

    protected GiraffeObserver(Set<Annotation> qualifiers) {
        this.qualifiers = qualifiers;
        reset();
    }

    protected GiraffeObserver(Annotation... qualifiers) {
        this(new HashSet<Annotation>(Arrays.asList(qualifiers)));
    }

    protected void reset() {
        this.legacyNotifyCalled = false;
        this.receivedPayload = null;
        this.receivedQualifiers = null;
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
        return TransactionPhase.IN_PROGRESS;
    }

    public void notify(Giraffe event) {
        legacyNotifyCalled = true;
    }

    public void notify(Giraffe event, Set<Annotation> qualifiers) {
        receivedPayload = event;
        receivedQualifiers = qualifiers;
    }

    public boolean isLegacyNotifyCalled() {
        return legacyNotifyCalled;
    }

    public Giraffe getReceivedPayload() {
        return receivedPayload;
    }

    public Set<Annotation> getReceivedQualifiers() {
        return receivedQualifiers;
    }
}