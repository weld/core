package org.jboss.weld.tests.beanDeployment.circular;

import jakarta.enterprise.inject.spi.DeploymentException;

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
public class DependentSelfInjectionTest {
    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(DependentSelfInjectionTest.class))
                .addClasses(Farm.class);
    }

    @Test
    public void testSelfInjectingBean() {
        // should throw deployment exception
    }

}
