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
package org.jboss.weld.experimental;

import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessObserverMethod;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link ProcessObserverMethod}.
 *
 * @author Jozef Hartinger
 * @see WELD-1749
 *
 * @param <T> The type of the event being observed
 * @param <X> The bean type containing the observer method
 */
public interface ExperimentalProcessObserverMethod<T, X> extends ProcessObserverMethod<T, X> {

    /**
     * Replaces the observer method.
     *
     * @param observer
     */
    void setObserverMethod(ObserverMethod<T> observerMethod);

    /**
     * Forces the container to ignore the observer method.
     */
    void veto();

    /**
     * Temporarilly overriden return type
     */
    @Override
    ExperimentalObserverMethod<T> getObserverMethod();
}
