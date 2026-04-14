package org.jboss.weld.tests.invokable.async.paramtype;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.invokable.async.DependentBean;

@ApplicationScoped
public class ParamTypeBean {
    public void hello(DependentBean dep, CompletableFuture<String> future, MyAsyncParam<String> async) {
        future.whenComplete((value, error) -> {
            if (error == null) {
                async.resume(value);
            }
        });
    }
}
