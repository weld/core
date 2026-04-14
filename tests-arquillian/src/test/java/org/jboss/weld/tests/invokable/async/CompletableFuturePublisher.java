package org.jboss.weld.tests.invokable.async;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple Flow.Publisher backed by a CompletableFuture.
 */
public final class CompletableFuturePublisher<T> implements Flow.Publisher<T> {
    private final CompletableFuture<T> future;

    public CompletableFuturePublisher(CompletableFuture<T> future) {
        this.future = Objects.requireNonNull(future);
    }

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        Objects.requireNonNull(subscriber);
        subscriber.onSubscribe(new CompletableFutureSubscription<>(future, subscriber));
    }

    private static final class CompletableFutureSubscription<T> implements Flow.Subscription {
        private static final int STATE_NEW = 0;
        private static final int STATE_PENDING = 1;
        private static final int STATE_FINISHED = 2;

        private final CompletableFuture<T> future;
        private final Flow.Subscriber<? super T> subscriber;
        private final AtomicInteger state = new AtomicInteger(STATE_NEW);

        CompletableFutureSubscription(CompletableFuture<T> future, Flow.Subscriber<? super T> subscriber) {
            this.future = future;
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            if (n <= 0L) {
                cancel();
                subscriber.onError(new IllegalArgumentException("Negative request: " + n));
                return;
            }

            if (state.compareAndSet(STATE_NEW, STATE_PENDING)) {
                future.whenComplete((value, error) -> {
                    if (state.compareAndSet(STATE_PENDING, STATE_FINISHED)) {
                        if (error != null) {
                            subscriber.onError(error);
                        } else if (value == null) {
                            subscriber.onError(new NullPointerException("CompletableFuture produced null"));
                        } else {
                            subscriber.onNext(value);
                            subscriber.onComplete();
                        }
                    }
                });
            }
        }

        @Override
        public void cancel() {
            if (state.getAndSet(STATE_FINISHED) != STATE_FINISHED) {
                future.cancel(false);
            }
        }
    }
}
