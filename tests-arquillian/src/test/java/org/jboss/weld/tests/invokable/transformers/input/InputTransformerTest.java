package org.jboss.weld.tests.invokable.transformers.input;

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
public class InputTransformerTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(InputTransformerTest.class))
                .addPackage(InputTransformerTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Inject
    ActualBean bean;

    @Test
    public void testArgTransformerAssignability() {
        Beta result;
        // test initial state without transformers
        result = (Beta) extension.getNoTransformer().invoke(bean, new Object[]{0});
        Assert.assertEquals("0", result.ping());
        Assert.assertEquals(Integer.valueOf(0), result.getInteger());

        // apply transformers, invoke method params are now String instead of original Number
        result = (Beta) extension.getTransformArg1().invoke(bean, new Object[]{"42"});
        Assert.assertEquals("42", result.ping());
        Assert.assertEquals(Integer.valueOf(0), result.getInteger());
        result = (Beta) extension.getTransformArg2().invoke(bean, new Object[]{"42"});
        Assert.assertEquals("42", result.ping());
        Assert.assertEquals(Integer.valueOf(0), result.getInteger());
    }

    @Test
    public void testInstanceTransformerAssignability() {
        Beta result;
        // test initial state without transformers
        result = (Beta) extension.getNoTransformer().invoke(bean, new Object[]{0});
        Assert.assertEquals("0", result.ping());
        Assert.assertEquals(Integer.valueOf(0), result.getInteger());

        // apply transformers, instance parameter is now null
        result = (Beta) extension.getTransformInstance1().invoke(null, new Object[]{42});
        Assert.assertEquals("42", result.ping());
        Assert.assertEquals(Integer.valueOf(100), result.getInteger());
        result = (Beta) extension.getTransformInstance2().invoke(null, new Object[]{42});
        Assert.assertEquals("42", result.ping());
        Assert.assertEquals(Integer.valueOf(100), result.getInteger());
    }
}
