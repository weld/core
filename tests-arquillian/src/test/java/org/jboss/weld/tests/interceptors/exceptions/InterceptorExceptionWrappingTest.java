package org.jboss.weld.tests.interceptors.exceptions;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * author Marko Luksa
 */
@RunWith(Arquillian.class)
public class InterceptorExceptionWrappingTest {
    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptorExceptionWrappingTest.class))
                .intercept(MyInterceptor.class)
                .addPackage(InterceptorExceptionWrappingTest.class.getPackage());
    }

    @Test(expected = FooCheckedException.class)
    public void testCheckedExceptionIsNotWrapped(Foo foo) throws Exception {
        foo.throwCheckedException();
    }

    @Test(expected = FooUncheckedException.class)
    public void testUncheckedExceptionIsNotWrapped(Foo foo) throws Exception {
        foo.throwUncheckedException();
    }
}
