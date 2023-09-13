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

import static org.jboss.weld.util.reflection.Reflections.cast;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.inject.spi.Interceptor;

/**
 * This implementation never invokes {@link PostConstruct} / {@link PreDestroy} callbacks. Useful for {@link Interceptor}
 * instances for example.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public class NoopLifecycleCallbackInvoker<T> implements LifecycleCallbackInvoker<T> {

    public static final NoopLifecycleCallbackInvoker<Object> INSTANCE = new NoopLifecycleCallbackInvoker<Object>();

    public static <T> NoopLifecycleCallbackInvoker<T> getInstance() {
        return cast(INSTANCE);
    }

    @Override
    public void postConstruct(T instance, Instantiator<T> instantiator) {
        // noop
    }

    @Override
    public void preDestroy(T instance, Instantiator<T> instantiator) {
        // noop
    }

    @Override
    public boolean hasPreDestroyMethods() {
        return true; // we cannot say no for sure
    }

    @Override
    public boolean hasPostConstructMethods() {
        return true; // we cannot say no for sure
    }

    @Override
    public boolean hasPostConstructCallback() {
        return false;
    }

}
