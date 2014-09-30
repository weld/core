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
package org.jboss.weld.experimental.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.weld.experimental.ExperimentalObserverMethod;

/**
 * This API is experimental and will change!
 *
 * This is a forwarding implementation of {@link ExperimentalObserverMethod}.
 *
 * @author Jozef Hartinger
 *
 */
public class ForwardingExperimentalObserverMethod<T> implements ExperimentalObserverMethod<T> {

    private final ObserverMethod<T> delegate;

    public ForwardingExperimentalObserverMethod(ObserverMethod<T> delegate) {
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

    @Override
    public int getPriority() {
        if (delegate() instanceof ExperimentalObserverMethod<?>) {
            return ((ExperimentalObserverMethod<?>) delegate()).getPriority();
        }
        return ExperimentalObserverMethod.DEFAULT_PRIORITY;
    }

    @Override
    public int hashCode() {
        return delegate().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForwardingExperimentalObserverMethod<?>) {
            obj = ((ForwardingExperimentalObserverMethod<?>) obj).delegate();
        }
        return delegate().equals(obj);
    }

    @Override
    public String toString() {
        return delegate().toString();
    }

}
