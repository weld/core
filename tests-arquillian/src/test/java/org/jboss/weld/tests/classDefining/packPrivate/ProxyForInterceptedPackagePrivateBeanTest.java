package org.jboss.weld.tests.classDefining.packPrivate;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.classDefining.packPrivate.api.Alpha;
import org.jboss.weld.tests.classDefining.packPrivate.interceptor.MyInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * See WELD-2758
 */
@RunWith(Arquillian.class)
public class ProxyForInterceptedPackagePrivateBeanTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap
                .create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyForInterceptedPackagePrivateBeanTest.class))
                .addPackages(true, ProxyForInterceptedPackagePrivateBeanTest.class.getPackage());
    }

    @Inject
    Instance<Object> instance;

    @Test
    public void testProxyCanBeCreated() {
        Instance<Alpha> select = instance.select(Alpha.class);
        Assert.assertTrue(select.isResolvable());
        Assert.assertEquals(MyInterceptor.class.getSimpleName() + Alpha.class.getSimpleName(), select.get().ping());
    }
}
