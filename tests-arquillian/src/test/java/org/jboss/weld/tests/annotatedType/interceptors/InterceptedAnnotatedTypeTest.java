package org.jboss.weld.tests.annotatedType.interceptors;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.Extension;
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
public class InterceptedAnnotatedTypeTest {

    @Inject
    @Default
    private Box defaultBox;

    @Inject
    @Additional
    private Box additionalBox;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InterceptedAnnotatedTypeTest.class))
                .intercept(BoxInterceptor.class).addPackage(InterceptedAnnotatedTypeTest.class.getPackage())
                .addAsServiceProvider(Extension.class, SetupExtension.class)
                .addClass(Utils.class);
    }

    @Test
    public void test() throws Exception {
        Assert.assertTrue(defaultBox.isIntercepted());
        Assert.assertFalse(additionalBox.isIntercepted());

        // test after deserialization

        Assert.assertTrue(Utils.<Box> deserialize(Utils.serialize(defaultBox)).isIntercepted());
        Assert.assertFalse(Utils.<Box> deserialize(Utils.serialize(additionalBox)).isIntercepted());
    }
}
