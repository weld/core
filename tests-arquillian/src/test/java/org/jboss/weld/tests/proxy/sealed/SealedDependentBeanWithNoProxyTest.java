package org.jboss.weld.tests.proxy.sealed;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.weld.test.util.Utils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SealedDependentBeanWithNoProxyTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(SealedDependentBeanWithNoProxyTest.class))
                .addClasses(MyDependent.class, MyDependentSubclass.class, InjectingBean3.class);
    }

    @Inject
    MyDependent bean;

    @Test
    public void testSealedDependentBeanWithNoProxyWorks() {
        // dependent bean with no proxyability requirement should work
        Assert.assertEquals(MyDependent.class.getSimpleName(), bean.ping());

    }
}
