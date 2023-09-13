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

package org.jboss.weld.bootstrap.events;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProcessSyntheticObserverMethod;

import org.jboss.weld.manager.BeanManagerImpl;

/**
 * This event is used to notify observers for each synthetic observer method that is added.
 *
 * @author Martin Kouba
 */
public class ProcessSyntheticObserverMethodImpl<T, X> extends ProcessObserverMethodImpl<T, X>
        implements ProcessSyntheticObserverMethod<T, X> {

    public static <T, X> ObserverMethod<T> fire(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod,
            ObserverMethod<T> observerMethod,
            Extension extension) {
        return fire(new ProcessSyntheticObserverMethodImpl<>(beanManager, beanMethod, observerMethod, extension));
    }

    private final Extension source;

    /**
     *
     * @param beanManager
     * @param beanMethod
     * @param observerMethod
     * @param extension
     */
    private ProcessSyntheticObserverMethodImpl(BeanManagerImpl beanManager, AnnotatedMethod<X> beanMethod,
            ObserverMethod<T> observerMethod,
            Extension extension) {
        super(beanManager, beanMethod, observerMethod, ProcessSyntheticObserverMethod.class);
        this.source = extension;
    }

    @Override
    public Extension getSource() {
        return source;
    }

}
