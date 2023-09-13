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
package org.jboss.weld.util;

import java.util.function.Supplier;

/**
 * Represents a lazily computed value.
 *
 * @author Stuart Douglas
 */
public abstract class LazyValueHolder<T> implements ValueHolder<T> {

    public static <T> LazyValueHolder<T> forSupplier(Supplier<T> supplier) {
        return new LazyValueHolder<T>() {
            @Override
            protected T computeValue() {
                return supplier.get();
            }
        };
    }

    private transient volatile T value;

    public T get() {
        T valueCopy = value;
        if (valueCopy != null) {
            return valueCopy;
        }
        synchronized (this) {
            if (value == null) {
                value = computeValue();
            }
            return value;
        }
    }

    @Override
    public T getIfPresent() {
        return value;
    }

    public void clear() {
        synchronized (this) {
            value = null;
        }
    }

    public boolean isAvailable() {
        return value != null;
    }

    protected abstract T computeValue();

    /**
     * {@link LazyValueHolder} that implements {@link java.io.Serializable}.
     *
     * @author Jozef Hartinger
     *
     * @param <T> the lazily-computed type
     */
    public abstract static class Serializable<T> extends LazyValueHolder<T> implements java.io.Serializable {

        private static final long serialVersionUID = 1L;
    }
}
