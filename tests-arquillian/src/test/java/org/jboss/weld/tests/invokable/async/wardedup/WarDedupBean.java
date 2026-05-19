package org.jboss.weld.tests.invokable.async.wardedup;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.invokable.async.DependentBean;

@ApplicationScoped
public class WarDedupBean {
    public MyAsyncType<String> hello(DependentBean dep, CompletableFuture<String> future) {
        return MyAsyncType.from(future);
    }
}
