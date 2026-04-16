package org.jboss.weld.tests.eager.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
public class EagerBeanTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(EagerBeanTest.class))
                .addPackage(EagerBeanTest.class.getPackage());
    }

    @Inject
    BeanContainer container;

    @Test
    public void testEagerBeanIsConstructedDuringStartup() {
        assertTrue("@Eager bean should be constructed during startup", EagerBean.constructed);
    }

    @Test
    public void testLazyBeanIsNotConstructedDuringStartup() {
        assertFalse("Lazy bean should not be constructed during startup", LazyBean.constructed);
        LazyBean ref = container.createInstance().select(LazyBean.class).get();
        assertNotNull(ref);
        assertNotNull(ref.toString()); // force proxy resolution
        assertTrue("Lazy bean should be constructed after access", LazyBean.constructed);
    }

    @Test
    public void testSingletonEagerBeanIsConstructedDuringStartup() {
        assertTrue("@Singleton @Eager bean should be constructed during startup",
                SingletonEagerBean.constructed);
    }

    @Test
    public void testEagerMetadata() {
        assertTrue("Eager bean should report isEager()=true",
                container.getBeans(EagerBean.class).iterator().next().isEager());
        assertTrue("Singleton eager bean should report isEager()=true",
                container.getBeans(SingletonEagerBean.class).iterator().next().isEager());
        assertFalse("Lazy bean should report isEager()=false",
                container.getBeans(LazyBean.class).iterator().next().isEager());
    }
}
