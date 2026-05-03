package org.jboss.weld.tests.invokable.async.broken;

import jakarta.enterprise.inject.spi.DefinitionException;
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
public class IndirectReturnTypeHandlerTest {

    @Deployment
    @ShouldThrowException(DefinitionException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class,
                Utils.getDeploymentNameAsHash(IndirectReturnTypeHandlerTest.class))
                .addClass(IndirectReturnTypeHandler.class)
                .addClass(IndirectReturnTypeBase.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, IndirectReturnTypeHandler.class);
    }

    @Test
    public void trigger() {
    }
}
