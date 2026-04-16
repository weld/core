package org.jboss.weld.tests.bce.syntheticInjectionPoint.disposer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.BeanContainer;
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
public class DisposerTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(DisposerTest.class))
                .addPackage(DisposerTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        DisposerExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testOldDisposerApi() {
        DisposableResult.reset();
        Instance<Object> instance = container.createInstance();
        Instance.Handle<DisposableResult> handle = instance
                .select(DisposableResult.class, OldDisposerQualifier.Literal.INSTANCE)
                .getHandle();
        assertNotNull(handle.get());
        assertFalse(DisposableResult.disposed);

        handle.destroy();
        assertTrue("Old API disposer should have been called", DisposableResult.disposed);
    }

    @Test
    public void testNewDisposerApi() {
        DisposableResult.reset();
        Instance<Object> instance = container.createInstance();
        Instance.Handle<DisposableResult> handle = instance
                .select(DisposableResult.class, NewDisposerQualifier.Literal.INSTANCE)
                .getHandle();
        assertNotNull(handle.get());
        assertFalse(DisposableResult.disposed);

        handle.destroy();
        assertTrue("New API disposer should have been called", DisposableResult.disposed);
    }
}
