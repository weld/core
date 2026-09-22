package org.jboss.weld.tests.invokable.async.matching;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import jakarta.annotation.PreDestroy;
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
import org.jboss.weld.bootstrap.event.WeldProcessManagedBean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class AsyncHandlerMatchingTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(AsyncHandlerMatchingTest.class.getDeclaredClasses())
                .addAsServiceProvider(Extension.class, Invokers.class)
                .addAsServiceProvider(AsyncHandler.ParameterType.class, AHandler.class, BHandler.class);
    }

    @Inject
    Invokers invokers;

    @Test
    public void returnAndParameterHandlersAreAmbiguous() throws Exception {
        Target.destroyed = 0;
        A a = new A();
        invokers.get("returnAndParameter").invoke(null, new Object[] { a });
        assertEquals(1, Target.destroyed);
        assertEquals(null, a.completion);
        Target.stage.complete("done");
        assertEquals(1, Target.destroyed);
    }

    @Test
    public void repeatedTypeDoesNotDisqualifyAnotherHandler() throws Exception {
        Target.destroyed = 0;
        A a = new A();
        B b = new B();
        invokers.get("repeatedAndUnique").invoke(null, new Object[] { a, a, b });
        assertEquals(0, Target.destroyed);
        assertEquals(null, a.completion);
        b.completion.run();
        assertEquals(1, Target.destroyed);
    }

    @Test
    public void distinctParameterHandlersAreAmbiguous() throws Exception {
        Target.destroyed = 0;
        A a = new A();
        B b = new B();
        invokers.get("distinct").invoke(null, new Object[] { a, b });
        assertEquals(1, Target.destroyed);
        assertEquals(null, a.completion);
        assertEquals(null, b.completion);
    }

    @Test
    public void repeatedParametersLeaveReturnHandlerUnambiguous() throws Exception {
        Target.destroyed = 0;
        A a = new A();
        invokers.get("returnAndRepeated").invoke(null, new Object[] { a, a });
        assertEquals(0, Target.destroyed);
        assertEquals(null, a.completion);
        Target.stage.complete("done");
        assertEquals(1, Target.destroyed);
    }

    public static class A {
        Runnable completion;
    }

    public static class B {
        Runnable completion;
    }

    public static class AHandler implements AsyncHandler.ParameterType<A> {
        public A transformArgument(A original, Runnable completion) {
            original.completion = completion;
            return original;
        }
    }

    public static class BHandler implements AsyncHandler.ParameterType<B> {
        public B transformArgument(B original, Runnable completion) {
            original.completion = completion;
            return original;
        }
    }

    @Dependent
    public static class Target {
        static int destroyed;
        static CompletableFuture<String> stage;

        public CompletionStage<String> returnAndParameter(A a) {
            stage = new CompletableFuture<>();
            return stage;
        }

        public void repeatedAndUnique(A a, A another, B b) {
        }

        public void distinct(A a, B b) {
        }

        public CompletionStage<String> returnAndRepeated(A a, A another) {
            stage = new CompletableFuture<>();
            return stage;
        }

        @PreDestroy
        void destroy() {
            destroyed++;
        }
    }

    public static class Invokers implements Extension {
        final Map<String, Invoker<Target, ?>> methods = new HashMap<>();

        public Invoker<Target, ?> get(String name) {
            return methods.get(name);
        }

        void register(@Observes WeldProcessManagedBean<Target> event) {
            event.getAnnotatedBeanClass().getMethods().stream()
                    .filter(method -> !method.getJavaMember().getName().equals("destroy"))
                    .forEach(method -> methods.put(method.getJavaMember().getName(),
                            event.createInvoker(method).withInstanceLookup().build()));
        }
    }
}
