package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.concurrent.atomic.AtomicReference;

public interface MyAsyncParam<T> {
    boolean isComplete();

    T getIfComplete();

    void whenComplete(Runnable callback);

    void resume(T value);

    static <T> MyAsyncParam<T> createSuspended() {
        return new MyAsyncParam<T>() {
            private final AtomicReference<T> value = new AtomicReference<>(null);
            private final AtomicReference<Runnable> callback = new AtomicReference<>(null);

            @Override
            public boolean isComplete() {
                return value.get() != null;
            }

            @Override
            public T getIfComplete() {
                T v = value.get();
                if (v != null) {
                    return v;
                }
                throw new IllegalStateException("not yet complete");
            }

            @Override
            public void whenComplete(Runnable cb) {
                callback.set(cb);
            }

            @Override
            public void resume(T val) {
                if (value.compareAndSet(null, val)) {
                    Runnable cb = callback.get();
                    if (cb != null) {
                        cb.run();
                    }
                }
            }
        };
    }
}
