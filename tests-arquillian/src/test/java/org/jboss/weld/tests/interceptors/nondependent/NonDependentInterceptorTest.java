package org.jboss.weld.tests.interceptors.nondependent;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.exceptions.DefinitionException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 */
@RunWith(Arquillian.class)
public class NonDependentInterceptorTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(NonDependentInterceptor.class)
                .addPackage(NonDependentInterceptorTest.class.getPackage());
    }

    @Test
    public void testDeploymentFails() {
    }
}
