/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.bootstrap.events;

import static org.jboss.weld.util.Observers.validateObserverMethod;

import java.lang.reflect.Type;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

import org.jboss.weld.experimental.ExperimentalProcessObserverMethod;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.Preconditions;
import org.jboss.weld.util.collections.WeldCollections;

/**
 * Implementation of the event used to notify observers for each observer
 * method that is added.
 *
 * @author David Allen
 */
public class ProcessObserverMethodImpl<T, X> extends AbstractDefinitionContainerEvent implements ProcessObserverMethod<T, X>, ExperimentalProcessObserverMethod<T, X> {

    public static <T, X> ObserverMethod<T> fire(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod, ObserverMethod<T> observerMethod) {
        ProcessObserverMethodImpl<T, X> event = new ProcessObserverMethodImpl<T, X>(beanManager, beanMethod, observerMethod) {};
        event.fire();
        if (event.vetoed) {
            return null;
        }
        if (event.isDirty()) {
            return event.observerMethod;
        }
        return observerMethod;
    }

    private final AnnotatedMethod<X> beanMethod;
    private final ObserverMethod<T> initialObserverMethod;
    private ObserverMethod<T> observerMethod;
    private boolean vetoed;

    private ProcessObserverMethodImpl(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod, ObserverMethod<T> observerMethod) {
        super(beanManager, ExperimentalProcessObserverMethod.class, new Type[]{observerMethod.getObservedType(), observerMethod.getBeanClass()});
        this.beanMethod = beanMethod;
        this.initialObserverMethod = observerMethod;
        this.observerMethod = observerMethod;
    }

    public AnnotatedMethod<X> getAnnotatedMethod() {
        checkWithinObserverNotification();
        return beanMethod;
    }

    public ObserverMethod<T> getObserverMethod() {
        checkWithinObserverNotification();
        return observerMethod;
    }

    public List<Throwable> getDefinitionErrors() {
        return WeldCollections.immutableListView(getErrors());
    }

    @Override
    public void setObserverMethod(ObserverMethod<T> observerMethod) {
        Preconditions.checkArgumentNotNull(observerMethod, "observerMethod");
        checkWithinObserverNotification();
        validateObserverMethod(observerMethod, getBeanManager(), initialObserverMethod);
        this.observerMethod = observerMethod;
    }

    @Override
    public void veto() {
        checkWithinObserverNotification();
        vetoed = true;
    }

    public boolean isDirty() {
        return observerMethod != initialObserverMethod;
    }

}
