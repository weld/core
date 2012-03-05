package org.jboss.weld.tests.interceptors.self;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Marius Bogoevici
 */
@RunWith(Arquillian.class)
public class SelfInvocationTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class)
                .intercept(SecuredInterceptor.class)
                .decorate(BeanDecorator.class)
                .addPackage(SelfInvocationTest.class.getPackage());
    }

    @Test
    public void testSelfInterception(Bean bean) {
        // safety-check: make sure that intercepted method is intercepted when invoked standalone
        SecuredInterceptor.reset();
        bean.doIntercepted();
        Assert.assertEquals(1, SecuredInterceptor.interceptedInvocations.size());
        Assert.assertTrue(SecuredInterceptor.interceptedInvocations.contains("doIntercepted"));

        // safety-check: make sure that decorated method is decorated when invoked standalone
        BeanDecorator.reset();
        bean.doDecorated();
        Assert.assertEquals(1, BeanDecorator.decoratedInvocationCount);

        // safety-check: make sure that intercepted and decorated methods are not intercepted when invoked from unintercepted method
        SecuredInterceptor.reset();
        BeanDecorator.reset();
        bean.doUnintercepted();
        Assert.assertEquals(0, SecuredInterceptor.interceptedInvocations.size());
        Assert.assertEquals(0, BeanDecorator.decoratedInvocationCount);
    }
}
