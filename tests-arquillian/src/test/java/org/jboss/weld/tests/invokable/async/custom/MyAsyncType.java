package org.jboss.weld.tests.invokable.async.custom;

import java.util.concurrent.CompletableFuture;

public interface MyAsyncType<T> {
    boolean isComplete();

    T getIfComplete();

    MyAsyncType<T> whenComplete(Runnable callback);

    static <T> MyAsyncType<T> from(CompletableFuture<T> future) {
        return new MyAsyncTypeImpl<>(future);
    }
}
