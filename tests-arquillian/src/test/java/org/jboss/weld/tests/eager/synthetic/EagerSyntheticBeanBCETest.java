package org.jboss.weld.tests.eager.synthetic;

import static org.junit.Assert.assertTrue;

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
public class EagerSyntheticBeanBCETest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(EagerSyntheticBeanBCETest.class))
                .addPackage(EagerSyntheticBeanBCETest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        EagerSyntheticBCExtension.class);
    }

    @Inject
    BeanContainer container;

    @Test
    public void testBCESyntheticEagerBeanIsCreatedDuringStartup() {
        assertTrue("BCE synthetic @Eager bean should be created during startup",
                EagerSyntheticBeanBCE.created);
    }

    @Test
    public void testBCESyntheticEagerBeanMetadata() {
        assertTrue("BCE synthetic @Eager bean should report isEager()=true",
                container.getBeans(EagerSyntheticBeanBCE.class).iterator().next().isEager());
    }
}
