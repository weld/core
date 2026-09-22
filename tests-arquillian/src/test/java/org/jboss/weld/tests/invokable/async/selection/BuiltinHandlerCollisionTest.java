package org.jboss.weld.tests.invokable.async.selection;

import java.util.concurrent.CompletionStage;

import jakarta.enterprise.inject.spi.DeploymentException;
import jakarta.enterprise.invoke.AsyncHandler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class BuiltinHandlerCollisionTest {
    @Deployment
    @ShouldThrowException(DeploymentException.class)
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClass(CustomStageHandler.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, CustomStageHandler.class);
    }

    @Test
    public void duplicateBuiltinRequiresSelection() {
    }

    public static class CustomStageHandler implements AsyncHandler.ReturnType<CompletionStage<?>> {
        public CompletionStage<?> transform(CompletionStage<?> original, Runnable completion) {
            return original.whenComplete((value, failure) -> completion.run());
        }
    }
}
