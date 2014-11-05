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
package org.jboss.weld.util;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

/**
 * {@link LazyValueHolder} that uses {@link WeakReference}.
 *
 * @author Jozef Hartinger
 *
 * @param <T>
 */
public abstract class WeakLazyValueHolder<T> implements ValueHolder<T> {

    public static <T> WeakLazyValueHolder<T> forSupplier(Supplier<T> supplier) {
        return new WeakLazyValueHolder<T>() {
            @Override
            protected T computeValue() {
                return supplier.get();
            }
        };
    }

    private volatile WeakReference<T> reference;

    public T get() {
        WeakReference<T> reference = this.reference;
        T value = null;
        if (reference != null) {
            value = reference.get();
        }
        if (value != null) {
            return value;
        }
        synchronized (this) {
            if (this.reference == reference) {
                T newValue = computeValue();
                this.reference = new WeakReference<T>(newValue);
            }
            return this.reference.get();
        }
    }

    @Override
    public T getIfPresent() {
        WeakReference<T> reference = this.reference;
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    public void clear() {
        synchronized (this) {
            reference = null;
        }
    }

    protected abstract T computeValue();
}
