package org.jboss.weld.tests.invokable.async.custom;

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

@RunWith(Arquillian.class)
public class CustomAsyncHandlerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(CustomAsyncHandlerTest.class))
                .addPackage(CustomAsyncHandlerTest.class.getPackage())
                .addClass(DependentBean.class)
                .addAsServiceProvider(Extension.class, CustomAsyncExtension.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, MyAsyncTypeHandler.class);
    }

    @Inject
    CustomAsyncExtension extension;

    @Test
    @SuppressWarnings("unchecked")
    public void testCustomReturnTypeHandler() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        MyAsyncType<String> result = (MyAsyncType<String>) extension.getInvoker()
                .invoke(null, new Object[] { null, future });

        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.isComplete());

        future.complete("custom");

        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isComplete());
        assertEquals("custom", result.getIfComplete());
    }
}
