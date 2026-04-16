package org.jboss.weld.tests.bce.syntheticInjectionPoint.broken;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.spi.DeploymentException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests withInjectionPoint(NoSuchBean.class) — no matching bean exists.
 */
@RunWith(Arquillian.class)
public class NoMatchingBeanTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(NoMatchingBeanTest.class))
                .addPackage(NoMatchingBeanTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        NoMatchingBeanExtension.class);
    }

    @Test
    public void trigger() {
    }
}
