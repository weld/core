package org.jboss.weld.tests.bce.syntheticInjectionPoint.dependentLifecycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

/**
 * Tests {@code @Dependent} bean lifecycle for beans obtained via
 * {@code SyntheticInjections} in synthetic bean creators and disposers.
 */
@RunWith(Arquillian.class)
public class DependentLifecycleTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(DependentLifecycleTest.class))
                .addPackage(DependentLifecycleTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        LifecycleExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testCreatorAndDisposerGetIndependentDependentInstances() {
        DependentCounter.reset();
        SyntheticResultHolder.reset();

        Instance<Object> instance = container.createInstance();
        Instance.Handle<SyntheticBean> handle = instance.select(SyntheticBean.class).getHandle();

        SyntheticBean result = handle.get();
        assertNotNull(result);

        // Creator should have obtained one DependentCounter
        assertEquals(1, DependentCounter.createdCounter.get());
        // It should still be alive (dependent object of synthetic bean)
        assertEquals(0, DependentCounter.destroyedCounter.get());

        // Now destroy — triggers disposer, then destroys creator's dependent
        handle.destroy();

        // Both creator and disposer should have obtained their own instance
        assertEquals(2, DependentCounter.createdCounter.get());
        // Creator and disposer must get different instances
        assertNotEquals("Creator and disposer must receive independent DependentCounter instances",
                SyntheticResultHolder.creatorCounterId, SyntheticResultHolder.disposerCounterId);
    }

    @Test
    public void testDisposerDependentDestroyedAfterDispose() {
        DependentCounter.reset();
        SyntheticResultHolder.reset();

        Instance<Object> instance = container.createInstance();
        Instance.Handle<SyntheticBean> handle = instance.select(SyntheticBean.class).getHandle();
        handle.get();

        assertEquals(0, DependentCounter.destroyedCounter.get());

        handle.destroy();

        // The disposer's @Dependent should have been destroyed after dispose() completed,
        // plus the creator's @Dependent should be destroyed as part of bean destruction.
        // Total: 2 created, 2 destroyed
        assertEquals(2, DependentCounter.createdCounter.get());
        assertEquals(2, DependentCounter.destroyedCounter.get());
    }

    @Test
    public void testCreatorDependentNotDestroyedBeforeDisposerRuns() {
        DependentCounter.reset();
        SyntheticResultHolder.reset();

        Instance<Object> instance = container.createInstance();
        Instance.Handle<SyntheticBean> handle = instance.select(SyntheticBean.class).getHandle();
        handle.get();

        handle.destroy();

        // When the disposer ran, the creator's dependent should not yet have been destroyed
        // (only 0 destroyed before the disposer called injections.get())
        assertEquals(0, SyntheticResultHolder.disposerDestroyedCountBeforeGet);
    }
}
