/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.injection.producer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Implementations of this interface are capable of performing {@link PostConstruct} / {@link PreDestroy} lifecycle callback
 * invocations.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public interface LifecycleCallbackInvoker<T> {

    void postConstruct(T instance, Instantiator<T> instantiator);

    void preDestroy(T instance, Instantiator<T> instantiator);

    boolean hasPreDestroyMethods();

    boolean hasPostConstructMethods();

    default boolean hasPostConstructCallback() {
        return hasPostConstructMethods();
    }

}
