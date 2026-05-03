package org.jboss.weld.tests.invokable.async.paramtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.AsyncHandler;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.invokable.async.DependentBean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ParameterTypeAsyncHandlerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(ParameterTypeAsyncHandlerTest.class))
                .addPackage(ParameterTypeAsyncHandlerTest.class.getPackage())
                .addClass(DependentBean.class)
                .addAsServiceProvider(Extension.class, ParamTypeExtension.class)
                .addAsServiceProvider(AsyncHandler.ParameterType.class, MyAsyncParamHandler.class);
    }

    @Inject
    ParamTypeExtension extension;

    @Test
    public void testTransformArgumentCalledBeforeMethodBody() throws Exception {
        DependentBean.reset();
        InvocationOrder.reset();
        CompletableFuture<String> future = new CompletableFuture<>();
        MyAsyncParam<String> result = MyAsyncParam.createSuspended();

        extension.getInvoker().invoke(null, new Object[] { null, future, result });

        assertEquals(Arrays.asList("transformArgument", "methodBody"), InvocationOrder.events);
        assertTrue("Method body should receive the wrapped argument from transformArgument",
                InvocationOrder.receivedWrapped);

        future.complete("order-test");
    }

    @Test
    public void testParameterTypeWithoutLookups() throws Exception {
        InvocationOrder.reset();
        CompletableFuture<String> future = new CompletableFuture<>();
        MyAsyncParam<String> result = MyAsyncParam.createSuspended();

        ParamTypeBean bean = new ParamTypeBean();
        extension.getNoLookupInvoker().invoke(bean, new Object[] { future, result });

        assertFalse(result.isComplete());

        future.complete("no-lookup-param");

        assertTrue(result.isComplete());
        assertEquals("no-lookup-param", result.getIfComplete());
    }

    @Test
    public void testSynchronousCompletion() throws Exception {
        DependentBean.reset();
        MyAsyncParam<String> result = MyAsyncParam.createSuspended();

        assertEquals(0, DependentBean.destroyedCounter.get());

        extension.getSyncInvoker().invoke(null, new Object[] { null, result });

        // Method completed the async param synchronously during the method body.
        // Cleanup must still happen — the completion callback must work even when
        // fired during mh.invoke().
        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isComplete());
        assertEquals("sync-hello", result.getIfComplete());
    }

    @Test
    public void testParameterTypeHandler() throws Exception {
        DependentBean.reset();
        InvocationOrder.reset();
        CompletableFuture<String> future = new CompletableFuture<>();
        MyAsyncParam<String> result = MyAsyncParam.createSuspended();

        assertEquals(0, DependentBean.destroyedCounter.get());

        extension.getInvoker().invoke(null, new Object[] { null, future, result });

        // Cleanup should be deferred
        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.isComplete());

        future.complete("param-hello");

        // Async param completion should trigger cleanup
        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isComplete());
        assertEquals("param-hello", result.getIfComplete());
    }
}
