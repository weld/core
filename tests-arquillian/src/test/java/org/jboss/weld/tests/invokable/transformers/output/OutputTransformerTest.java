package org.jboss.weld.tests.invokable.transformers.output;

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
public class OutputTransformerTest {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(BeanArchive.class, Utils.getDeploymentNameAsHash(OutputTransformerTest.class))
                .addPackage(OutputTransformerTest.class.getPackage())
                .addAsServiceProvider(Extension.class, ObservingExtension.class);
    }

    @Inject
    ObservingExtension extension;

    @Inject
    ActualBean bean;

    @Inject
    ExceptionalBean exceptionalBean;

    @Test
    public void testReturnTypeTransformerAssignability() {
        Beta betaResult;
        // test initial state without transformers
        betaResult = (Beta) extension.getNoTransformer().invoke(bean, new Object[] { 0 });
        Assert.assertEquals("0", betaResult.ping());
        Assert.assertEquals(Integer.valueOf(0), betaResult.getInteger());

        // apply transformers, first one returns Beta, the other just String
        Object result;
        result = extension.getTransformReturnType1().invoke(bean, new Object[] { 10 });
        Assert.assertTrue(result instanceof Beta);
        Assert.assertEquals("42", ((Beta) result).ping());
        Assert.assertEquals(Integer.valueOf(42), ((Beta) result).getInteger());
        result = extension.getTransformReturnType2().invoke(bean, new Object[] { 23 });
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals("230", result.toString());
    }

    @Test
    public void testExceptionTransformerAssignability() {
        // apply transformers, first one swallows exception and returns Beta
        Object result;
        result = extension.getTransformException1().invoke(exceptionalBean, new Object[] { 10 });
        Assert.assertTrue(result instanceof Beta);
        Assert.assertEquals("42", ((Beta) result).ping());
        Assert.assertEquals(Integer.valueOf(42), ((Beta) result).getInteger());
        // second transformer returns a subclas
        result = extension.getTransformException2().invoke(exceptionalBean, new Object[] { 23 });
        Assert.assertTrue(result instanceof Gamma);
        Assert.assertEquals("42", ((Gamma) result).ping());
        Assert.assertEquals(Integer.valueOf(42), ((Gamma) result).getInteger());
        // third transformer returns a completely different type
        result = extension.getTransformException3().invoke(exceptionalBean, new Object[] { 23 });
        Assert.assertTrue(result instanceof String);
        Assert.assertEquals("foobar", result);
    }
}
