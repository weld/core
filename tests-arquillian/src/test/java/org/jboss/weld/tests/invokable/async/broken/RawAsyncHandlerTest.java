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
public class RawAsyncHandlerTest {

    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(RawAsyncHandlerTest.class))
                .addClass(RawAsyncHandler.class)
                .addAsServiceProvider(AsyncHandler.class, RawAsyncHandler.class);
    }

    @Test
    public void trigger() {
    }
}
