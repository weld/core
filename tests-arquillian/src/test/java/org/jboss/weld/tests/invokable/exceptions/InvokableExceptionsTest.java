package org.jboss.weld.tests.invokable.exceptions;

import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.invoke.Invoker;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InvokableExceptionsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableExceptionsTest.class))
                .addPackage(InvokableExceptionsTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Inject
    ExceptionTestBean bean;

    @Test
    public void testNullPrimitiveArgument() {
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { "foo", null }),
                NullPointerException.class,
                "WELD-002019");

        assertException(() -> extension.getStaticPingInvoker().invoke(null, new Object[] { "foo", null }),
                NullPointerException.class,
                "WELD-002019");

        assertException(() -> extension.getVoidPingInvoker().invoke(bean, new Object[] { "foo", null }),
                NullPointerException.class,
                "WELD-002019");
    }

    @Test
    public void testWrongReferenceArgumentType() {
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { new Object(), 2 }),
                ClassCastException.class,
                "WELD-002018");

        assertException(() -> extension.getStaticPingInvoker().invoke(bean, new Object[] { new Object(), 2 }),
                ClassCastException.class,
                "WELD-002018");

        assertException(() -> extension.getVoidPingInvoker().invoke(bean, new Object[] { new Object(), 2 }),
                ClassCastException.class,
                "WELD-002018");
    }

    @Test
    public void testWrongPrimitiveArgumentType() throws Exception {
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { "foo", new Object() }),
                ClassCastException.class,
                "WELD-002018");

        assertException(() -> extension.getStaticPingInvoker().invoke(bean, new Object[] { "foo", new Object() }),
                ClassCastException.class,
                "WELD-002018");

        assertException(() -> extension.getVoidPingInvoker().invoke(bean, new Object[] { "foo", new Object() }),
                ClassCastException.class,
                "WELD-002018");

        // Narrowing conversion results in ClassCastException
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { "foo", 4L }),
                ClassCastException.class,
                "WELD-002018");

        // Widening version is permitted
        extension.getPingInvoker().invoke(bean, new Object[] { "foo", (short) 4 });
    }

    @Test
    public void testNullArgumentsArray() throws Exception {
        assertException(() -> extension.getPingInvoker().invoke(bean, null),
                NullPointerException.class,
                "WELD-002020");

        assertException(() -> extension.getStaticPingInvoker().invoke(bean, null),
                NullPointerException.class,
                "WELD-002020");

        assertException(() -> extension.getVoidPingInvoker().invoke(bean, null),
                NullPointerException.class,
                "WELD-002020");

        // Args array can be null if the method has no parameters
        extension.getNoargPingInvoker().invoke(bean, null);
    }

    @Test
    public void testNullInstance() {
        assertException(() -> extension.getPingInvoker().invoke(null, new Object[] { "foo", 2 }),
                NullPointerException.class,
                "WELD-002015");

        assertException(() -> extension.getVoidPingInvoker().invoke(null, new Object[] { "foo", 2 }),
                NullPointerException.class,
                "WELD-002015");

        assertException(() -> extension.getNoargPingInvoker().invoke(null, new Object[] {}),
                NullPointerException.class,
                "WELD-002015");
    }

    @Test
    public void testMissingArguments() {
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { "foo" }),
                IllegalArgumentException.class,
                "WELD-002017");
        assertException(() -> extension.getStaticPingInvoker().invoke(bean, new Object[] { "foo" }),
                IllegalArgumentException.class,
                "WELD-002017");
        assertException(() -> extension.getVoidPingInvoker().invoke(bean, new Object[] { "foo" }),
                IllegalArgumentException.class,
                "WELD-002017");
    }

    @Test
    public void testWrongInstanceType() throws Exception {
        Invoker<Object, ?> pingInvoker = (Invoker<Object, ?>) (Invoker<?, ?>) extension.getPingInvoker();
        assertException(() -> pingInvoker.invoke(new Object(), new Object[] { "foo", 2 }),
                ClassCastException.class,
                "WELD-002016");
        Invoker<Object, ?> voidPingInvoker = (Invoker<Object, ?>) (Invoker<?, ?>) extension.getVoidPingInvoker();
        assertException(() -> voidPingInvoker.invoke(new Object(), new Object[] { "foo", 2 }),
                ClassCastException.class,
                "WELD-002016");
        Invoker<Object, ?> noargPingInvoker = (Invoker<Object, ?>) (Invoker<?, ?>) extension.getVoidPingInvoker();
        assertException(() -> noargPingInvoker.invoke(new Object(), new Object[] {}),
                ClassCastException.class,
                "WELD-002016");

        // Anything can be passed as the instance to a static method
        Invoker<Object, ?> staticPingInvoker = (Invoker<Object, ?>) (Invoker<?, ?>) extension.getStaticPingInvoker();
        staticPingInvoker.invoke(new Object(), new Object[] { "foo", 2 });
    }

    @Test
    public void testCustomNumberArgument() {
        class MyCustomThree extends Number {

            private static final long serialVersionUID = 3L;

            @Override
            public double doubleValue() {
                return 3d;
            }

            @Override
            public float floatValue() {
                return 3f;
            }

            @Override
            public int intValue() {
                return 3;
            }

            @Override
            public long longValue() {
                return 3L;
            }
        }

        // Check that a custom Number implementation cannot be unboxed and so results in a ClassCastException
        assertException(() -> extension.getPingInvoker().invoke(bean, new Object[] { "foo", new MyCustomThree() }),
                ClassCastException.class,
                "WELD-002018");
    }

    private void assertException(ThrowingRunnable runnable, Class<? extends Exception> exceptionType, String messageContains) {
        try {
            runnable.run();
            Assert.fail("No exception thrown");
        } catch (Exception e) {
            if (!exceptionType.isInstance(e)) {
                throw new AssertionError("Expected " + exceptionType + " but got " + e, e);
            }
            Assert.assertTrue("Exception message did not contain " + messageContains, e.getMessage().contains(messageContains));
        }
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
