package org.jboss.weld.tests.invokable.async.custom;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.invokable.async.DependentBean;

@ApplicationScoped
public class CustomAsyncBean {
    public MyAsyncType<String> hello(DependentBean dep, CompletableFuture<String> future) {
        return MyAsyncType.from(future);
    }
}
