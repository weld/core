package org.jboss.weld.tests.eager.broken.stereotype;

import jakarta.enterprise.inject.spi.DefinitionException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class EagerAndDependentOnStereotypeTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(EagerAndDependentOnStereotypeTest.class))
                .addPackage(EagerAndDependentOnStereotypeTest.class.getPackage());
    }

    @Test
    public void trigger() {
        // deployment should fail with DefinitionException
    }
}
