package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.invokable.async.DependentBean;

@ApplicationScoped
public class MultipleParamMatchBean {
    public static boolean futureComplete = false;

    public void hello(DependentBean dep, CompletableFuture<String> future,
            MyAsyncParam<String> async1, MyAsyncParam<String> async2) {
        future.whenComplete((value, error) -> {
            futureComplete = true;
        });
    }
}
