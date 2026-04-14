package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.invoke.AsyncHandler;

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
public class UnannotatedHandlerTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(UnannotatedHandlerTest.class))
                .addClass(UnannotatedHandler.class)
                .addAsServiceProvider(AsyncHandler.class, UnannotatedHandler.class);
    }

    @Test
    public void trigger() {
    }
}
