package org.jboss.weld.tests.invokable.async.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
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
 * Tests async invoker built via Build Compatible Extension, passed as a
 * synthetic bean parameter, and validated via InvokerValidation.
 */
@RunWith(Arquillian.class)
public class CustomAsyncBCETest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(CustomAsyncBCETest.class))
                .addPackage(CustomAsyncBCETest.class.getPackage())
                .addClass(DependentBean.class)
                .addAsServiceProvider(BuildCompatibleExtension.class, CustomAsyncBCExtension.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, MyAsyncTypeHandler.class);
    }

    @Inject
    CustomAsyncBCExtension.AsyncInvokerHolder holder;

    @Test
    @SuppressWarnings("unchecked")
    public void testAsyncInvokerViaBCE() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        MyAsyncType<String> result = (MyAsyncType<String>) holder.getInvoker()
                .invoke(null, new Object[] { null, future });

        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.isComplete());

        future.complete("bce-async");

        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isComplete());
        assertEquals("bce-async", result.getIfComplete());
    }
}
