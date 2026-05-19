package org.jboss.weld.tests.invokable.async.wardedup;

import java.util.concurrent.CompletableFuture;

final class MyAsyncTypeImpl<T> implements MyAsyncType<T> {
    private final CompletableFuture<T> future;
    private Runnable callback;

    MyAsyncTypeImpl(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public boolean isComplete() {
        return future.isDone();
    }

    @Override
    public T getIfComplete() {
        if (future.isDone()) {
            return future.getNow(null);
        }
        throw new IllegalStateException("not yet complete");
    }

    @Override
    public MyAsyncType<T> whenComplete(Runnable callback) {
        this.callback = callback;
        future.whenComplete((v, e) -> {
            if (this.callback != null) {
                this.callback.run();
            }
        });
        return this;
    }
}
