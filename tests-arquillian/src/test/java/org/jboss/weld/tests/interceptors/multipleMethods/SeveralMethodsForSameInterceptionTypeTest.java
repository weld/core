package org.jboss.weld.tests.interceptors.multipleMethods;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SeveralMethodsForSameInterceptionTypeTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static JavaArchive getDeployment() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(SeveralMethodsForSameInterceptionTypeTest.class))
                .addPackage(SeveralMethodsForSameInterceptionTypeTest.class.getPackage());
    }

    @Test
    public void testMultipleMethodsAreDetected() {
        // no-op, test should throw exception
    }
}
