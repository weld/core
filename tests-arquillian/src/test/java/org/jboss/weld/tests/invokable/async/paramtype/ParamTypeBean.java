package org.jboss.weld.tests.invokable.async.paramtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.weld.tests.invokable.async.DependentBean;

@ApplicationScoped
public class ParamTypeBean {
    public void helloNoLookup(CompletableFuture<String> future, MyAsyncParam<String> async) {
        future.whenComplete((value, error) -> {
            if (error == null) {
                async.resume(value);
            }
        });
    }

    public void helloSync(DependentBean dep, MyAsyncParam<String> async) {
        async.resume("sync-hello");

        // completion was signaled, but the method didn't return yet,
        // so the dependency must not be destroyed
        assertEquals(0, DependentBean.destroyedCounter.get());
    }

    public void hello(DependentBean dep, CompletableFuture<String> future, MyAsyncParam<String> async) {
        InvocationOrder.events.add("methodBody");
        InvocationOrder.receivedWrapped = async instanceof WrappedAsyncParam;
        future.whenComplete((value, error) -> {
            if (error == null) {
                async.resume(value);
            }
        });
    }

    public void helloThrow(DependentBean dep, CompletableFuture<String> future, MyAsyncParam<String> async) {
        future.whenComplete((value, error) -> {
            assertNull(error);
            async.resume(value);
        });
        throw new IllegalArgumentException("synchronous throw");
    }
}
