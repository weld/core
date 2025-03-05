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
import org.jboss.weld.tests.invokable.common.SimpleBean;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InvokableExceptionsTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InvokableExceptionsTest.class))
                .addPackage(InvokableExceptionsTest.class.getPackage())
                .addPackage(SimpleBean.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Inject
    SimpleBean bean;

    @Test(expected = NullPointerException.class)
    public void testNullPrimitiveArgument() throws Exception {
        extension.getInvoker().invoke(bean, new Object[] { "foo", null });
    }

    @Test(expected = ClassCastException.class)
    public void testWrongArgumentType() throws Exception {
        extension.getInvoker().invoke(bean, new Object[] { new Object(), 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArgumentsArray() throws Exception {
        extension.getInvoker().invoke(bean, null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullInstance() throws Exception {
        extension.getInvoker().invoke(null, new Object[] { "foo", 2 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingArguments() throws Exception {
        extension.getInvoker().invoke(bean, new Object[] { "foo" });
    }

    @Test(expected = ClassCastException.class)
    public void testWrongInstanceType() throws Exception {
        Invoker<Object, ?> invoker = (Invoker<Object, ?>) (Invoker<?, ?>) extension.getInvoker();
        invoker.invoke(new Object(), new Object[] { "foo", 2 });
    }
}
