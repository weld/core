package org.jboss.weld.tests.proxy.ignoreinvalidmethods.inheritance;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.test.util.Utils;
import org.jboss.weld.tests.util.PropertiesBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A bean class implementing a hierarchy of abstract classes where one introduces a method and the other implements it
 * as final. This prevents proxying, but we should still be able to ignore such method when creating proxy.
 *
 * See WELD-2785
 */
@RunWith(Arquillian.class)
public class ProxyIgnoreInvalidInheritedMethodsTest {

    @Deployment
    public static Archive<?> createTestArchive() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(ProxyIgnoreInvalidInheritedMethodsTest.class))
                .addPackage(ProxyIgnoreInvalidInheritedMethodsTest.class.getPackage())
                .addAsResource(PropertiesBuilder.newBuilder()
                        .set(ConfigurationKey.PROXY_IGNORE_FINAL_METHODS.get(),
                                ImplBean.class.getName() + "|" + AbstractSuperClass2.class.getName())
                        .build(), "weld.properties");
    }

    @Inject
    ImplBean implBean;

    @Test
    public void testProxy() {
        // firstly, the test should be able to deploy and execute, i.e. to create the proxy
        // then we verify that interception happens only for one of methods
        Assert.assertEquals(0, SecureInterceptor.timesInvoked);
        implBean.pong();
        Assert.assertEquals(1, SecureInterceptor.timesInvoked);
        implBean.ping();
        Assert.assertEquals(1, SecureInterceptor.timesInvoked);
    }

}
