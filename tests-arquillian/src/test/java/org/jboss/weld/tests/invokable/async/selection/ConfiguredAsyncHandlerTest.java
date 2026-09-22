package org.jboss.weld.tests.invokable.async.selection;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.AsyncHandler;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ConfiguredAsyncHandlerTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(ConfiguredAsyncHandlerTest.class.getDeclaredClasses())
                .addAsServiceProvider(Extension.class, Invokers.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, CustomStageHandler.class)
                .addAsResource(new StringAsset("org.jboss.weld.invokable.asyncHandlers="
                        + CompletionStage.class.getName() + "=" + CustomStageHandler.class.getName()), "weld.properties");
    }

    @Inject
    Invokers invokers;

    @Test
    public void configuredProviderTransformsInvocationResult() throws Exception {
        assertEquals("custom:value", ((CompletionStage<?>) invokers.get().invoke(null, new Object[0]))
                .toCompletableFuture().join());
    }

    @Dependent
    public static class Target {
        public CompletionStage<String> execute() {
            return CompletableFuture.completedFuture("value");
        }
    }

    public static class CustomStageHandler implements AsyncHandler.ReturnType<CompletionStage<?>> {
        public CompletionStage<?> transform(CompletionStage<?> original, Runnable completion) {
            return original.whenComplete((value, failure) -> completion.run()).thenApply(value -> "custom:" + value);
        }
    }

    public static class Invokers implements Extension {
        private Invoker<Target, ?> invoker;

        public Invoker<Target, ?> get() {
            return invoker;
        }

        void register(@Observes WeldProcessManagedBean<Target> event) {
            event.getAnnotatedBeanClass().getMethods().forEach(method -> invoker = event.createInvoker(method)
                    .withInstanceLookup().build());
        }
    }
}
