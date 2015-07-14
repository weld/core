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
package org.jboss.weld.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.event.FireAsyncException;

import org.jboss.weld.util.ForwardingCompletionStage;

/**
 * TODO Find a better way to propagate FireAsyncException or unwrap CompletionException.
 *
 * @param <T>
 */
public class AsyncEventDeliveryStage<T> extends ForwardingCompletionStage<T> {

    private final CompletionStage<T> delegate;

    AsyncEventDeliveryStage(Supplier<T> supplier, Executor executor) {
        this.delegate = CompletableFuture.supplyAsync(supplier, executor);
    }

    @Override
    protected CompletionStage<T> delegate() {
        return delegate;
    }

    @Override
    public CompletionStage<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return super.whenComplete((r,t) -> action.accept(r, unwrap(t)));
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return super.whenCompleteAsync((r,t) -> action.accept(r, unwrap(t)));
    }

    @Override
    public CompletionStage<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return super.whenCompleteAsync((r,t) -> action.accept(r, unwrap(t)), executor);
    }

    @Override
    public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return super.handle((r,t) -> fn.apply(r, unwrap(t)));
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return super.handleAsync((r,t) -> fn.apply(r, unwrap(t)));
    }

    @Override
    public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return super.handleAsync((r,t) -> fn.apply(r, unwrap(t)), executor);
    }

    @Override
    public CompletionStage<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return super.exceptionally((t) -> fn.apply(unwrap(t)));
    }

    private static Throwable unwrap(Throwable exception) {
        if (exception != null) {
            if (exception instanceof CompletionException) {
                exception = ((CompletionException) exception).getCause();
            }
            if (!(exception instanceof FireAsyncException)) {
                exception = new FireAsyncException(exception);
            }
        }
        return exception;
    }
}
