package org.jboss.weld.tests.producer.field;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.Instance;
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

/**
 * Field producer should not be inherited into bean subclass. In this scenario, a CDI extension modifies the type
 * which leads to a registration of a new annotated type that we need to correctly parse.
 *
 * See https://issues.redhat.com/browse/WELD-2773
 */
@RunWith(Arquillian.class)
public class FieldProducerExtensionTest {

    @Deployment
    public static Archive<?> deploy() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(FieldProducerExtensionTest.class))
                .addPackage(FieldProducerExtensionTest.class.getPackage())
                .addAsServiceProvider(Extension.class, MyExtension.class);
    }

    @Inject
    Instance<Object> instance;

    @Inject
    Foo foo;

    @Test
    public void test() {
        // assert extension works
        Assert.assertEquals(2, MyExtension.extensionTriggered);
        // assert producer works
        Assert.assertNotNull(foo);

        // assert both beans are there
        List<? extends Instance.Handle<FieldProducerBean>> collect = instance.select(FieldProducerBean.class).handlesStream()
                .collect(Collectors.toList());
        Assert.assertEquals(2, collect.size());
    }

    public static class Foo {
    }
}
