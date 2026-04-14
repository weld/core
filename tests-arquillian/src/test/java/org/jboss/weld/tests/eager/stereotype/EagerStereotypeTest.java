package org.jboss.weld.tests.eager.stereotype;

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
public class EagerStereotypeTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(EagerStereotypeTest.class))
                .addPackage(EagerStereotypeTest.class.getPackage());
    }

    @Inject
    BeanContainer container;

    @Test
    public void testEagerViaStereotype() {
        assertTrue("Bean with @Eager stereotype should be constructed during startup",
                StereotypeEagerBean.constructed);
    }

    @Test
    public void testEagerViaStereotypeMetadata() {
        assertTrue("Bean with @Eager stereotype should report isEager()=true",
                container.getBeans(StereotypeEagerBean.class).iterator().next().isEager());
    }

    @Test
    public void testEagerViaInheritedStereotype() {
        assertTrue("Bean with inherited @Eager stereotype should be constructed during startup",
                InheritedStereotypeEagerBean.constructed);
    }

    @Test
    public void testEagerViaInheritedStereotypeMetadata() {
        assertTrue("Bean with inherited @Eager stereotype should report isEager()=true",
                container.getBeans(InheritedStereotypeEagerBean.class).iterator().next().isEager());
    }
}
