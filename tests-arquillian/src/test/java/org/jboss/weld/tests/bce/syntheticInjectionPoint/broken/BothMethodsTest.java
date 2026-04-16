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
 * SyntheticBeanCreator implements both create(Instance, Parameters)
 * and create(SyntheticInjections, Parameters) — should fail deployment.
 */
@RunWith(Arquillian.class)
public class BothMethodsTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(BothMethodsTest.class))
                .addPackage(BothMethodsTest.class.getPackage())
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        BothMethodsExtension.class);
    }

    @Test
    public void trigger() {
    }
}
