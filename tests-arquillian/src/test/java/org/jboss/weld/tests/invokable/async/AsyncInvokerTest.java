package org.jboss.weld.tests.invokable.async;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AsyncInvokerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(AsyncInvokerTest.class))
                .addPackage(AsyncInvokerTest.class.getPackage())
                .addAsServiceProvider(Extension.class, AsyncInvokerExtension.class);
    }

    @Inject
    AsyncInvokerExtension extension;

    @Test
    @SuppressWarnings("unchecked")
    public void testCompletionStageReturnType() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        CompletionStage<String> result = (CompletionStage<String>) extension.getCsInvoker()
                .invoke(null, new Object[] { null, future });

        // Cleanup should be deferred — dependent bean not yet destroyed
        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.toCompletableFuture().isDone());

        future.complete("hello");

        // Now cleanup should have happened
        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.toCompletableFuture().isDone());
        assertEquals("hello", result.toCompletableFuture().getNow(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCompletableFutureReturnType() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        CompletableFuture<String> result = (CompletableFuture<String>) extension.getCfInvoker()
                .invoke(null, new Object[] { null, future });

        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.isDone());

        future.complete("world");

        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isDone());
        assertEquals("world", result.getNow(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReturnTypeWithoutLookups() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();

        AsyncBean bean = new AsyncBean();
        CompletionStage<String> result = (CompletionStage<String>) extension.getNoLookupInvoker()
                .invoke(bean, new Object[] { future });

        assertFalse(result.toCompletableFuture().isDone());

        future.complete("no-lookup");

        assertTrue(result.toCompletableFuture().isDone());
        assertEquals("no-lookup", result.toCompletableFuture().getNow(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFlowPublisherReturnType() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        Flow.Publisher<String> result = (Flow.Publisher<String>) extension.getFpInvoker()
                .invoke(null, new Object[] { null, future });

        AtomicReference<String> value = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean done = new AtomicBoolean(false);

        result.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(1);
            }

            @Override
            public void onNext(String item) {
                value.set(item);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onComplete() {
                done.set(true);
            }
        });

        assertEquals(0, DependentBean.destroyedCounter.get());
        assertNull(value.get());
        assertFalse(done.get());

        future.complete("publisher");

        assertEquals(1, DependentBean.destroyedCounter.get());
        assertEquals("publisher", value.get());
        assertNull(error.get());
        assertTrue(done.get());
    }
}
