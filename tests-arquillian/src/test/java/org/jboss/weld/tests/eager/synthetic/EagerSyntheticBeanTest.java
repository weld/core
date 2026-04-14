package org.jboss.weld.tests.eager.synthetic;

import static org.junit.Assert.assertTrue;

import jakarta.enterprise.inject.spi.BeanContainer;
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
public class EagerSyntheticBeanTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(EagerSyntheticBeanTest.class))
                .addPackage(EagerSyntheticBeanTest.class.getPackage())
                .addAsServiceProvider(Extension.class, EagerSyntheticBeanExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testSyntheticEagerBeanIsCreatedDuringStartup() {
        assertTrue("Synthetic @Eager bean should be created during startup",
                EagerSyntheticBean.created);
    }

    @Test
    public void testSyntheticEagerBeanMetadata() {
        assertTrue("Synthetic @Eager bean should report isEager()=true",
                container.getBeans(EagerSyntheticBean.class).iterator().next().isEager());
    }
}
