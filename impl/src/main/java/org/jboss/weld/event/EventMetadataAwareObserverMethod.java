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
package org.jboss.weld.event;

import jakarta.enterprise.inject.spi.EventMetadata;
import jakarta.enterprise.inject.spi.ObserverMethod;

/**
 * Marker interface for observer methods which are able to decide whether an access to {@link EventMetadata} is required or not.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public interface EventMetadataAwareObserverMethod<T> extends ObserverMethod<T> {

    /**
     *
     * @return <code>true</code> if {@link EventMetadata} is required, <code>false</code> otherwise
     */
    boolean isEventMetadataRequired();

}
