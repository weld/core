package org.jboss.weld.tests.invokable.async.paramtype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

/**
 * When multiple parameters match a ParameterType handler, the method
 * is NOT async — cleanup must happen synchronously.
 */
@RunWith(Arquillian.class)
public class MultipleParamMatchTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(MultipleParamMatchTest.class))
                .addClasses(MultipleParamMatchBean.class, MultipleParamMatchExtension.class,
                        MyAsyncParam.class, MyAsyncParamHandler.class, InvocationOrder.class,
                        WrappedAsyncParam.class, DependentBean.class)
                .addAsServiceProvider(Extension.class, MultipleParamMatchExtension.class)
                .addAsServiceProvider(AsyncHandler.ParameterType.class, MyAsyncParamHandler.class);
    }

    @Inject
    MultipleParamMatchExtension extension;

    @Test
    public void testMultipleMatchingParamsNotAsync() throws Exception {
        DependentBean.reset();
        MultipleParamMatchBean.futureComplete = false;
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        extension.getInvoker().invoke(null, new Object[] { null, future, null, null });

        // Not async — cleanup should happen synchronously
        assertFalse(MultipleParamMatchBean.futureComplete);
        assertEquals(1, DependentBean.destroyedCounter.get());

        future.complete("hello");

        assertTrue(MultipleParamMatchBean.futureComplete);
        assertEquals(1, DependentBean.destroyedCounter.get());
    }
}
