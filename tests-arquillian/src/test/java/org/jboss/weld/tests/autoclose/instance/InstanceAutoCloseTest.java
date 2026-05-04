package org.jboss.weld.tests.autoclose.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.BeanContainer;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.ActionSequence;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InstanceAutoCloseTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(InstanceAutoCloseTest.class))
                .addPackage(InstanceAutoCloseTest.class.getPackage())
                .addClass(ActionSequence.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testHandleDestroyCallsClose() {
        SimpleAutoCloseableBean.reset();
        Instance<Object> instance = container.createInstance();
        Instance.Handle<SimpleAutoCloseableBean> handle = instance
                .select(SimpleAutoCloseableBean.class).getHandle();
        handle.get();
        assertFalse(SimpleAutoCloseableBean.closed);

        handle.destroy();

        assertTrue("close() should be called via Handle.destroy()",
                SimpleAutoCloseableBean.closed);
    }

    @Test
    public void testHandleDestroyCallsCloseAndPreDestroy() {
        ActionSequence.reset();
        Instance<Object> instance = container.createInstance();
        Instance.Handle<PreDestroyAutoCloseableBean> handle = instance
                .select(PreDestroyAutoCloseableBean.class).getHandle();
        handle.get();

        handle.destroy();

        ActionSequence.assertSequenceDataEquals("preDestroy", "close");
    }

    @Test
    public void testCloseCalledExactlyOnce() {
        CloseCountingBean.reset();
        Instance<Object> instance = container.createInstance();
        Instance.Handle<CloseCountingBean> handle = instance
                .select(CloseCountingBean.class).getHandle();
        handle.get();

        handle.destroy();

        assertEquals("close() should be called exactly once", 1, CloseCountingBean.closeCount.get());
    }
}
