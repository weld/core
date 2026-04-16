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
import org.jboss.weld.tests.bce.syntheticInjectionPoint.basic.MyQualifier;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Bean exists with @Default but not with @MyQualifier.
 */
@RunWith(Arquillian.class)
public class WrongQualifierTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(WrongQualifierTest.class))
                .addPackage(WrongQualifierTest.class.getPackage())
                .addClass(MyQualifier.class)
                .addAsServiceProvider(BuildCompatibleExtension.class,
                        WrongQualifierExtension.class);
    }

    @Test
    public void trigger() {
    }
}
