package org.jboss.weld.tests.invokable.async.wardedup;

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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.category.Integration;
import org.jboss.weld.tests.invokable.async.DependentBean;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * Verifies that async handler discovery works in a WAR with multiple BDAs
 * sharing the same classloader. The service file in WEB-INF/classes is visible
 * to all BDAs, so the handler must be deduplicated rather than rejected.
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class WarDedupAsyncHandlerTest {

    @Deployment
    public static Archive<?> deploy() {
        // The library JAR creates a second BDA within the WAR. Both BDAs share
        // the WAR classloader, so async handler discovery (which runs per BDA)
        // finds the same META-INF/services/ file twice.
        JavaArchive lib = ShrinkWrap.create(BeanArchive.class)
                .addClass(LibraryBean.class);

        return ShrinkWrap
                .create(WebArchive.class, Utils.getDeploymentNameAsHash(WarDedupAsyncHandlerTest.class, Utils.ARCHIVE_TYPE.WAR))
                .addClasses(WarDedupBean.class, WarDedupExtension.class,
                        MyAsyncType.class, MyAsyncTypeImpl.class, MyAsyncTypeHandler.class,
                        DependentBean.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsServiceProvider(Extension.class, WarDedupExtension.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, MyAsyncTypeHandler.class)
                .addAsLibrary(lib);
    }

    @Inject
    WarDedupExtension extension;

    @Test
    @SuppressWarnings("unchecked")
    public void testCustomReturnTypeHandlerInWar() throws Exception {
        DependentBean.reset();
        CompletableFuture<String> future = new CompletableFuture<>();

        assertEquals(0, DependentBean.destroyedCounter.get());

        MyAsyncType<String> result = (MyAsyncType<String>) extension.getInvoker()
                .invoke(null, new Object[] { null, future });

        assertEquals(0, DependentBean.destroyedCounter.get());
        assertFalse(result.isComplete());

        future.complete("dedup");

        assertEquals(1, DependentBean.destroyedCounter.get());
        assertTrue(result.isComplete());
        assertEquals("dedup", result.getIfComplete());
    }
}
