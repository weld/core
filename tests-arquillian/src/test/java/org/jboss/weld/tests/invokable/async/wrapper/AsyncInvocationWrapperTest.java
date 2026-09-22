package org.jboss.weld.tests.invokable.async.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class AsyncInvocationWrapperTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .addClasses(AsyncInvocationWrapperTest.class.getDeclaredClasses())
                .addAsServiceProvider(Extension.class, Invokers.class)
                .addAsServiceProvider(AsyncHandler.ParameterType.class, Handler.class)
                .addAsServiceProvider(AsyncHandler.ReturnType.class, ReturnHandler.class);
    }

    @Inject
    Invokers invokers;

    @Test
    public void wrappedLookupWaitsForCompletion() throws Exception {
        Target.destroyed = 0;
        Param param = new Param();
        assertEquals("wrapped:handled:ok", invokers.get("execute").invoke(null, new Object[] { param }));
        assertEquals(0, Target.destroyed);
        param.completions.get(0).run();
        assertEquals(1, Target.destroyed);
    }

    @Test
    public void wrapperWorksWithoutLookup() throws Exception {
        Target.destroyed = 0;
        Param param = new Param();
        assertEquals("wrapped:handled:ok", invokers.get("direct").invoke(new Target(), new Object[] { param }));
        param.completions.get(0).run();
        assertEquals(0, Target.destroyed);
    }

    @Test
    public void synchronousFailureCleansUpOnlyOnce() {
        Target.destroyed = 0;
        Param param = new Param();
        assertThrows(IllegalArgumentException.class, () -> invokers.get("fail").invoke(null, new Object[] { param }));
        assertEquals(1, Target.destroyed);
        param.completions.get(0).run();
        assertEquals(1, Target.destroyed);
    }

    @Test
    public void repeatedDelegationHasIndependentCleanup() throws Exception {
        Target.destroyed = 0;
        Param param = new Param();
        assertEquals("handled:ok", invokers.get("twice").invoke(null, new Object[] { param }));
        assertEquals(2, param.completions.size());
        assertEquals(0, Target.destroyed);
        param.completions.get(0).run();
        assertEquals(1, Target.destroyed);
        param.completions.get(1).run();
        assertEquals(2, Target.destroyed);
    }

    @Test
    public void returnHandlerTransformsBeforeWrapperWithoutLookup() throws Exception {
        Target.destroyed = 0;
        assertEquals("wrapped:handled:ok", invokers.get("returnDirect").invoke(new Target(), new Object[0]));
        assertEquals(0, Target.destroyed);
    }

    public static class Param {
        final List<Runnable> completions = new ArrayList<>();
    }

    public static class Handler implements AsyncHandler.ParameterType<Param> {
        public Param transformArgument(Param original, Runnable completion) {
            original.completions.add(completion);
            return original;
        }

        @Override
        public Object transformReturnValue(Object original, Runnable completion) {
            return "handled:" + original;
        }
    }

    public static class ReturnValue {
        final String value;

        ReturnValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class ReturnHandler implements AsyncHandler.ReturnType<ReturnValue> {
        @Override
        public ReturnValue transform(ReturnValue original, Runnable completion) {
            completion.run();
            return new ReturnValue("handled:" + original.value);
        }
    }

    public static class Wrapper {
        public static Object once(Target instance, Object[] args, Invoker<Target, ?> invoker) throws Exception {
            return "wrapped:" + invoker.invoke(instance, args);
        }

        public static Object twice(Target instance, Object[] args, Invoker<Target, ?> invoker) throws Exception {
            invoker.invoke(instance, args);
            return invoker.invoke(instance, args);
        }
    }

    @Dependent
    public static class Target {
        static int destroyed;

        public String execute(Param param) {
            return "ok";
        }

        public String fail(Param param) {
            throw new IllegalArgumentException("expected");
        }

        public ReturnValue returnValue() {
            return new ReturnValue("ok");
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
            event.getAnnotatedBeanClass().getMethods().forEach(method -> {
                String name = method.getJavaMember().getName();
                if (name.equals("destroy")) {
                    return;
                }
                methods.put(name, event.createInvoker(method).withInstanceLookup()
                        .withInvocationWrapper(Wrapper.class, "once").build());
                if (name.equals("execute")) {
                    methods.put("direct", event.createInvoker(method).withInvocationWrapper(Wrapper.class, "once").build());
                    methods.put("twice", event.createInvoker(method).withInstanceLookup()
                            .withInvocationWrapper(Wrapper.class, "twice").build());
                } else if (name.equals("returnValue")) {
                    methods.put("returnDirect", event.createInvoker(method)
                            .withInvocationWrapper(Wrapper.class, "once").build());
                }
            });
        }
    }
}
