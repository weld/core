package org.jboss.weld.tests.invokable.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AsyncBean {

    public CompletionStage<String> helloCS(DependentBean dep, CompletableFuture<String> future) {
        return helloCF(dep, future);
    }

    public CompletableFuture<String> helloCF(DependentBean dep, CompletableFuture<String> future) {
        CompletableFuture<String> result = new CompletableFuture<>();
        future.whenComplete((value, error) -> {
            if (error == null) {
                result.complete(value);
            } else {
                result.completeExceptionally(error);
            }
        });
        return result;
    }

    public CompletionStage<String> helloNoLookup(CompletableFuture<String> future) {
        CompletableFuture<String> result = new CompletableFuture<>();
        future.whenComplete((value, error) -> {
            if (error == null) {
                result.complete(value);
            } else {
                result.completeExceptionally(error);
            }
        });
        return result;
    }

    public Flow.Publisher<String> helloFP(DependentBean dep, CompletableFuture<String> future) {
        return new CompletableFuturePublisher<>(future);
    }
}
