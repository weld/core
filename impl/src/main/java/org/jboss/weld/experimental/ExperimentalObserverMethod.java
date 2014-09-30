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
import javax.interceptor.Interceptor;

/**
 * This API is experimental and will change! All the methods declared by this interface are supposed to be moved to {@link ObserverMethod}.
 *
 * @author Jozef Hartinger
 * @see WELD-1728
 *
 * @param <T> the event type
 */
public interface ExperimentalObserverMethod<T> extends ObserverMethod<T>, Prioritized {

    /**
     * Default priority for observer methods that do not define priority. Currently the default priority is set to be in the middle
     * of the "APPLICATION" range (see {@link Interceptor.Priority} for details).
     *
     * WARNING: This concept of default priority of an observer method is preliminary and subject to change.
     */
    int DEFAULT_PRIORITY = 2500;

    /**
     * The priority of this observer method
     */
    default int getPriority() {
        return DEFAULT_PRIORITY;
    }
}
